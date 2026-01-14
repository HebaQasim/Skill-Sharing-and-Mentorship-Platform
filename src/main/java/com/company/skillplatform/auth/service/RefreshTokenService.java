package com.company.skillplatform.auth.service;

import com.company.skillplatform.auth.entity.RefreshToken;
import com.company.skillplatform.user.entity.User;

import java.util.UUID;

public interface RefreshTokenService {

    String create(User user, String deviceId, String deviceInfo);

    RefreshToken validate(String token, String deviceId);

    void revoke(UUID userId, String deviceId);

    void revokeAll(UUID userId);
}

