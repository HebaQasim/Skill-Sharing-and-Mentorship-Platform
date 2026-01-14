package com.company.skillplatform.auth.controller;

import com.company.skillplatform.auth.dto.*;
import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestPart("data") RegisterRequest request,
            @RequestPart("profileImage") MultipartFile profileImage
    ) {
        RegisterResponse response = authService.register(request, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public TokenResponse login(
            @Valid @RequestBody LoginRequest request,
            @NotBlank @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        return authService.login(request, deviceId, userAgent);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                authService.refresh(
                        request.refreshToken(),
                        request.deviceId()
                )
        );
    }


    @PostMapping("/logout")
    public void logout(
            @AuthenticationPrincipal UserPrincipal user,
            @NotBlank @RequestHeader("X-Device-Id") String deviceId
    ) {
        authService.logout(user.getId(), deviceId);
    }

    @PostMapping("/logout-all")
    public void logoutAll(@AuthenticationPrincipal UserPrincipal user) {
        authService.logoutAll(user.getId());
    }
}

