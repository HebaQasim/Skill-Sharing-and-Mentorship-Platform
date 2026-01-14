package com.company.skillplatform.post.service;

import com.company.skillplatform.post.dto.PostAttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface PostAttachmentManageService {
    PostAttachmentResponse add(UUID userId, UUID postId, MultipartFile file);
    void delete(UUID userId, UUID postId, UUID attachmentId);
}
