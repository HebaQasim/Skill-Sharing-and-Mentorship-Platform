package com.company.skillplatform.post.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.*;
import com.company.skillplatform.post.service.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostCommentController {

    private final PostCommentService service;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> add(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.add(user.getId(), postId, request));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<CursorPageResponse<CommentResponse>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(service.list(user.getId(), postId, limit, cursor));
    }

    @PatchMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> edit(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @Valid @RequestBody EditCommentRequest request
    ) {
        return ResponseEntity.ok(service.edit(user.getId(), postId, commentId, request));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @PathVariable UUID commentId
    ) {
        service.delete(user.getId(), postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
