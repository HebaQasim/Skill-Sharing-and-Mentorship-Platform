package com.company.skillplatform.post.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.post.dto.UpdatePublishedPostRequest;
import com.company.skillplatform.post.service.PostPublishedService;
import com.company.skillplatform.post.service.PostSoftDeleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostPublishedController {

    private final PostPublishedService service;
private final PostSoftDeleteService postSoftDeleteService;
    @PatchMapping("/{postId}")
    public ResponseEntity<Void> updatePublishedContent(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePublishedPostRequest request
    ) {
        service.updateContent(user.getId(), postId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal user,
                                       @PathVariable UUID postId) {
        postSoftDeleteService.softDelete(user.getId(), postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/restore")
    public ResponseEntity<Void> restore(@AuthenticationPrincipal UserPrincipal user,
                                        @PathVariable UUID postId) {
        postSoftDeleteService
                .restore(user.getId(), postId);
        return ResponseEntity.noContent().build();
    }

}
