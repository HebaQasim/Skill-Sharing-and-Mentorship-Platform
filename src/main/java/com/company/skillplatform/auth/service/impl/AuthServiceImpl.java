package com.company.skillplatform.auth.service.impl;

import com.company.skillplatform.auth.dto.RegisterResponse;
import com.company.skillplatform.auth.dto.LoginRequest;
import com.company.skillplatform.auth.dto.RegisterRequest;
import com.company.skillplatform.auth.dto.TokenResponse;
import com.company.skillplatform.auth.entity.Role;
import com.company.skillplatform.auth.enums.RoleName;
import com.company.skillplatform.auth.mapper.AuthMapper;
import com.company.skillplatform.auth.repository.RoleRepository;
import com.company.skillplatform.auth.service.AuthService;
import com.company.skillplatform.auth.service.JwtService;
import com.company.skillplatform.auth.service.RefreshTokenService;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.exception.BusinessException;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.UnauthorizedException;
import com.company.skillplatform.common.storage.StorageResult;
import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.user.service.impl.EmployeeDirectoryServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CacheStampService cacheStampService;
    private final StorageService storageService;

    @Override
    public RegisterResponse register(RegisterRequest request, MultipartFile profileImage) {
        log.info("Registration attempt for email={}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: email already exists={}", request.email());
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already registered");
        }
        if (profileImage == null || profileImage.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Profile image is required");
        }
        validateProfileImage(profileImage);

        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseThrow(() -> {
                    log.error("EMPLOYEE role not found");
                    return new BusinessException(ErrorCode.ROLE_NOT_FOUND, "Default role not found");
                });
        StorageResult stored = storageService.store("profile-images", profileImage);
        String imageKey = stored.storageKey();

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .department(request.department())
                .jobTitle(request.jobTitle())
                .phoneNumber(request.phoneNumber())
                .profileImageUrl(imageKey)
                .enabled(true)
                .mentor(false)
                .build();

        user.getRoles().add(employeeRole);

        User savedUser = userRepository.save(user);
        cacheStampService.bump(EmployeeDirectoryServiceImpl.EMP_DIR_STAMP);

        log.info("User registered successfully: id={} email={}", savedUser.getId(), savedUser.getEmail());

        return authMapper.toAuthResponse(savedUser, savedUser.getRoles().iterator().next().getName().name());
    }

    @Override
    public TokenResponse login(LoginRequest request, String deviceId, String deviceInfo) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found email={}", request.email());
                    return new UnauthorizedException("Invalid credentials");
                });

        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "Account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: invalid password for email={}", request.email());
            throw new UnauthorizedException("Invalid credentials");
        }

        log.info("Login successful for email={} deviceId={}", user.getEmail(), deviceId);
        return issueTokens(user, deviceId, deviceInfo);
    }

    @Override
    public TokenResponse refresh(String refreshToken, String deviceId) {
        var stored = refreshTokenService.validate(refreshToken, deviceId);
        refreshTokenService.revoke(stored.getUser().getId(), deviceId);
        log.info("Refreshing tokens for userId={} deviceId={}", stored.getUser().getId(), deviceId);
        return issueTokens(stored.getUser(), deviceId, stored.getDeviceInfo());
    }


    @Override
    public void logout(UUID userId, String deviceId) {
        refreshTokenService.revoke(userId, deviceId);
        log.info("User logged out userId={} deviceId={}", userId, deviceId);
    }

    @Override
    public void logoutAll(UUID userId) {
        refreshTokenService.revokeAll(userId);
        log.info("User logged out from all devices userId={}", userId);
    }

    @Value("${security.jwt.access-expiration}")
    private long accessExpirationMs;

    private TokenResponse issueTokens(User user, String deviceId, String deviceInfo) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        String accessToken = jwtService.generate(user.getId(), user.getEmail(), roles);
        String refreshToken = refreshTokenService.create(user, deviceId, deviceInfo);

        log.debug("Issued accessToken and refreshToken for userId={} deviceId={}", user.getId(), deviceId);
        return new TokenResponse(accessToken, refreshToken, accessExpirationMs / 1000);
    }

    private void validateProfileImage(MultipartFile file) {

        long maxBytes = 2L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Profile image is too large (max 2MB)");
        }

        String ct = file.getContentType();
        if (ct == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Profile image content type is required");
        }

        boolean ok = ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp");
        if (!ok) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Only JPEG/PNG/WEBP images are allowed");
        }
    }
}
