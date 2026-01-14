package com.company.skillplatform.session.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedbackResponse(
        UUID id,
        UUID sessionId,
        Integer rating,
        String comment,
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
