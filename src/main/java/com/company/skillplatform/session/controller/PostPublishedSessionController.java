package com.company.skillplatform.session.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.session.dto.SessionResponse;
import com.company.skillplatform.session.dto.UpdatePublishedSessionRequest;
import com.company.skillplatform.session.service.PostPublishedSessionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostPublishedSessionController {

    private final PostPublishedSessionService service;

    @PutMapping("/{postId}/session")
    public ResponseEntity<SessionResponse> upsertOrUpdate(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePublishedSessionRequest request
    ) {
        return ResponseEntity.ok(service.upsertOrUpdate(user.getId(), postId, request));
    }

    @DeleteMapping("/{postId}/session")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        service.cancel(user.getId(), postId);
        return ResponseEntity.noContent().build();
    }
}
