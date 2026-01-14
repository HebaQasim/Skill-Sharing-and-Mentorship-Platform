package com.company.skillplatform.session.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.session.dto.*;
import com.company.skillplatform.session.service.SessionFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionFeedbackController {

    private final SessionFeedbackService service;

    @PostMapping("/{sessionId}/feedback")
    public ResponseEntity<FeedbackResponse> add(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId,
            @Valid @RequestBody CreateFeedbackRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.add(user.getId(), sessionId, request));
    }

    @GetMapping("/{sessionId}/feedback")
    public ResponseEntity<CursorPageResponse<FeedbackResponse>> list(
            @PathVariable UUID sessionId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(service.list(sessionId, limit, cursor));
    }

    @PatchMapping("/{sessionId}/feedback/{feedbackId}")
    public ResponseEntity<FeedbackResponse> edit(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId,
            @PathVariable UUID feedbackId,
            @Valid @RequestBody EditFeedbackRequest request
    ) {
        return ResponseEntity.ok(service.edit(user.getId(), sessionId, feedbackId, request));
    }

    @DeleteMapping("/{sessionId}/feedback/{feedbackId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId,
            @PathVariable UUID feedbackId
    ) {
        service.delete(user.getId(), sessionId, feedbackId);
        return ResponseEntity.noContent().build();
    }
}
