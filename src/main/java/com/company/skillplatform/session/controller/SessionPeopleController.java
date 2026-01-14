package com.company.skillplatform.session.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.session.dto.SessionPersonRow;
import com.company.skillplatform.session.service.SessionPeopleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionPeopleController {

    private final SessionPeopleService service;

    @GetMapping("/{sessionId}/registered")
    public ResponseEntity<List<SessionPersonRow>> registered(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(service.registered(user.getId(), sessionId));
    }

    @GetMapping("/{sessionId}/attended")
    public ResponseEntity<List<SessionPersonRow>> attended(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(service.attended(user.getId(), sessionId));
    }
}
