package com.company.skillplatform.auth.service.impl;

import com.company.skillplatform.auth.entity.RefreshToken;
import com.company.skillplatform.auth.repository.RefreshTokenRepository;
import com.company.skillplatform.auth.service.RefreshTokenService;
import com.company.skillplatform.common.exception.UnauthorizedException;
import com.company.skillplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${security.jwt.refresh-expiration}")
    private long refreshExpirationMs;
    @Override
    public String create(User user, String deviceId, String deviceInfo) {

        String jti = UUID.randomUUID().toString();
        String rawToken = jti + "." + UUID.randomUUID(); // token has stable id + random part

        repository.save(RefreshToken.builder()
                .jti(jti)
                .hashedToken(hash(rawToken))
                .user(user)
                .deviceId(deviceId)
                .deviceInfo(deviceInfo)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build()
        );
        log.debug("Created refresh token for userId={} deviceId={}", user.getId(), deviceId);
        return rawToken;
    }


    @Override
    public RefreshToken validate(String token, String deviceId) {
        RefreshToken stored = repository
                .findByHashedTokenAndDeviceId(hash(token), deviceId)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Refresh token expired or revoked for userId={} deviceId={}", stored.getUser().getId(), deviceId);
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        return stored;
    }

    @Override
    public void revoke(UUID userId, String deviceId) {
        repository.revokeByUserAndDevice(userId, deviceId);
        log.info("Revoked refresh token for userId={} deviceId={}", userId, deviceId);
    }

    @Override
    public void revokeAll(UUID userId) {
        repository.revokeAllByUser(userId);
        log.info("Revoked all refresh tokens for userId={}", userId);
    }

    private String hash(String token) {
        return DigestUtils.sha256Hex(token);
    }
}
