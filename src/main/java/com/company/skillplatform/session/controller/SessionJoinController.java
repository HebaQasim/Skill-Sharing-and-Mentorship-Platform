package com.company.skillplatform.session.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.session.dto.JoinSessionResponse;
import com.company.skillplatform.session.service.SessionJoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionJoinController {

    private final SessionJoinService joinService;

    @PostMapping("/{sessionId}/join")
    public ResponseEntity<JoinSessionResponse> join(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId
    ) {
        return ResponseEntity.ok(joinService.join(user.getId(), sessionId));
    }
}
