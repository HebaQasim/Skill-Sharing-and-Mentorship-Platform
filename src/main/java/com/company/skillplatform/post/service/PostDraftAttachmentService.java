package com.company.skillplatform.post.service;

import com.company.skillplatform.post.dto.PostAttachmentResponse;

import java.util.List;
import java.util.UUID;

public interface PostDraftAttachmentService {

    PostAttachmentResponse add(UUID userId, UUID postId, org.springframework.web.multipart.MultipartFile file);

    void remove(UUID userId, UUID postId, UUID attachmentId);

    List<PostAttachmentResponse> list(UUID userId, UUID postId);
}
