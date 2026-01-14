package com.company.skillplatform.post.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.post.dto.PostAttachmentResponse;
import com.company.skillplatform.post.service.PostDraftAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostDraftAttachmentController {

    private final PostDraftAttachmentService service;

    @PostMapping(value = "/{postId}/draft/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostAttachmentResponse> add(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.add(user.getId(), postId, file));
    }

    @GetMapping("/{postId}/draft/attachments")
    public ResponseEntity<List<PostAttachmentResponse>> list(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId
    ) {
        return ResponseEntity.ok(service.list(user.getId(), postId));
    }

    @DeleteMapping("/{postId}/draft/attachments/{attachmentId}")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @PathVariable UUID attachmentId
    ) {
        service.remove(user.getId(), postId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
