package com.company.skillplatform.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DraftPostListItemResponse(
        UUID id,
        String title,
        String bodyPreview,
        LocalDateTime createdAt,
        LocalDateTime editedAt
) {}

