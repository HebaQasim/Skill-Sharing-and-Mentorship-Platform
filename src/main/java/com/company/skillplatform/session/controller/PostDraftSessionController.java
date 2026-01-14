package com.company.skillplatform.session.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.session.dto.SessionResponse;
import com.company.skillplatform.session.dto.UpsertDraftSessionRequest;
import com.company.skillplatform.session.service.PostDraftSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostDraftSessionController {

    private final PostDraftSessionService service;

    @PutMapping("/{postId}/draft/session")
    public ResponseEntity<SessionResponse> upsert(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @Valid @RequestBody UpsertDraftSessionRequest request
    ) {
        return ResponseEntity.ok(service.upsert(user.getId(), postId, request));
    }

    @DeleteMapping("/{postId}/draft/session")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        service.remove(user.getId(), postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/draft/session")
    public ResponseEntity<SessionResponse> get(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        return service.get(user.getId(), postId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}

