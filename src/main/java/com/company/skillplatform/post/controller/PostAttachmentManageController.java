package com.company.skillplatform.post.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.post.dto.PostAttachmentResponse;
import com.company.skillplatform.post.service.PostAttachmentManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostAttachmentManageController {

    private final PostAttachmentManageService service;

    @PostMapping(value = "/{postId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostAttachmentResponse> add(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.add(user.getId(), postId, file));
    }

    @DeleteMapping("/{postId}/attachments/{attachmentId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID postId,
            @PathVariable UUID attachmentId
    ) {
        service.delete(user.getId(), postId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
