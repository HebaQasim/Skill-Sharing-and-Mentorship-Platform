package com.company.skillplatform.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostAttachmentResponse(
        UUID id,
        String type,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String downloadUrl,
        LocalDateTime createdAt
) {}

