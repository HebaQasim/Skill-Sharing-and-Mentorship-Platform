package com.company.skillplatform.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        String body,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        boolean deleted,

        UUID authorId,
        String authorFullName,
        String authorDepartment,
        String authorJobTitle,
        String authorHeadline,
        String authorProfileImageUrl
) {}

