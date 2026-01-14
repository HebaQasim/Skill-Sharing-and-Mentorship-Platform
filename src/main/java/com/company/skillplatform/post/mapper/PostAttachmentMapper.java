package com.company.skillplatform.post.mapper;

import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.post.dto.PostAttachmentResponse;
import com.company.skillplatform.post.entity.PostAttachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostAttachmentMapper {

    private final StorageService storageService;

    public PostAttachmentResponse toResponse(PostAttachment a) {
        return new PostAttachmentResponse(
                a.getId(),
                a.getType().name(),
                a.getOriginalFilename(),
                a.getContentType(),
                a.getSizeBytes(),
                storageService.signedUrl(a.getStorageKey()),
                a.getCreatedAt()
        );
    }
}

