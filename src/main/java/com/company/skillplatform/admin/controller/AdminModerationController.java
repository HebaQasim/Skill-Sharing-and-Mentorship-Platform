package com.company.skillplatform.admin.controller;

import com.company.skillplatform.admin.service.AdminModerationService;
import com.company.skillplatform.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/moderation")
@PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_ADMIN')")
public class AdminModerationController {

    private final AdminModerationService moderationService;

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        moderationService.deletePost(user, postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @PathVariable UUID commentId
    ) {
        moderationService.deleteComment(user, postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sessions/{sessionId}/feedback/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId,
            @PathVariable UUID feedbackId
    ) {
        moderationService.deleteFeedback(user, sessionId, feedbackId);
        return ResponseEntity.noContent().build();
    }
}

