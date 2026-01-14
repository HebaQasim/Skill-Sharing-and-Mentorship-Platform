package com.company.skillplatform.user.service.impl;

import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.user.service.UserPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPasswordServiceImpl implements UserPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void changePassword(UUID userId, String currentPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));


        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Current password is incorrect"
            );
        }


        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "New password must be different from current password"
            );
        }


        user.setPassword(passwordEncoder.encode(newPassword));
    }
}
