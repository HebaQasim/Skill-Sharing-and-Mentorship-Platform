package com.company.skillplatform.post.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.DraftPostListItemResponse;
import com.company.skillplatform.post.dto.PostDraftResponse;
import com.company.skillplatform.post.dto.UpdatePostDraftRequest;
import com.company.skillplatform.post.service.PostDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostDraftController {

    private final PostDraftService postDraftService;

    @PostMapping("/drafts")
    public ResponseEntity<PostDraftResponse> createDraft(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postDraftService.createDraft(user.getId()));
    }
    @GetMapping("/drafts")
    public ResponseEntity<CursorPageResponse<DraftPostListItemResponse>> myDrafts(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(postDraftService.myDrafts(user.getId(), limit, cursor));
    }
    @GetMapping("/{postId}/draft")
    public ResponseEntity<PostDraftResponse> getDraft(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(
                postDraftService.getDraft(user.getId(), postId)
        );
    }


    @PatchMapping("/{postId}/draft")
    public ResponseEntity<PostDraftResponse> updateDraft(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostDraftRequest request
    ) {
        return ResponseEntity.ok(postDraftService.updateDraft(user.getId(), postId, request));
    }


    @PostMapping("/{postId}/publish")
    public ResponseEntity<Void> publish(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        postDraftService.publish(user.getId(), postId);
        return ResponseEntity.noContent().build();
    }

}

