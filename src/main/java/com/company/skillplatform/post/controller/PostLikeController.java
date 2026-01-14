package com.company.skillplatform.post.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.PostLikerResponse;
import com.company.skillplatform.post.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PutMapping("/{postId}/like")
    public ResponseEntity<Void> like(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        postLikeService.like(user.getId(), postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> unlike(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        postLikeService.unlike(user.getId(), postId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/likers")
    public ResponseEntity<CursorPageResponse<PostLikerResponse>> likers(
            @PathVariable UUID postId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(postLikeService.likers(postId, limit, cursor));
    }
}

