package com.company.skillplatform.post.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.PostCardResponse;
import com.company.skillplatform.post.dto.PostDetailsResponse;
import com.company.skillplatform.post.service.PostFeedService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostFeedController {

    private final PostFeedService postFeedService;

    // Home feed
    @GetMapping("/feed")
    public ResponseEntity<CursorPageResponse<PostCardResponse>> feed(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(postFeedService.feed(user.getId(), limit, cursor));
    }

    // Open full post when clicking the card
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailsResponse> getPost(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(postFeedService.getPost(user.getId(), postId));
    }

}

