package com.company.skillplatform.auth.service;

import com.company.skillplatform.auth.dto.RegisterResponse;
import com.company.skillplatform.auth.dto.LoginRequest;
import com.company.skillplatform.auth.dto.RegisterRequest;
import com.company.skillplatform.auth.dto.TokenResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AuthService {

    RegisterResponse register(RegisterRequest request, MultipartFile profileImage);

    TokenResponse login(LoginRequest request, String deviceId, String deviceInfo);

    TokenResponse refresh(String refreshToken, String deviceId);

    void logout(UUID userId, String deviceId);

    void logoutAll(UUID userId);
}
