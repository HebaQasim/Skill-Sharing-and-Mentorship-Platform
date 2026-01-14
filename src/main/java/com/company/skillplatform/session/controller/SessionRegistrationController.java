package com.company.skillplatform.session.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.session.dto.SessionRegistrationResponse;
import com.company.skillplatform.session.service.SessionRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionRegistrationController {

    private final SessionRegistrationService service;

    @PostMapping("/{sessionId}/registrations")
    public ResponseEntity<SessionRegistrationResponse> register(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.register(user.getId(), sessionId));
    }

    @DeleteMapping("/{sessionId}/registrations/me")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId
    ) {
        service.cancel(user.getId(), sessionId);
        return ResponseEntity.noContent().build();
    }
}
