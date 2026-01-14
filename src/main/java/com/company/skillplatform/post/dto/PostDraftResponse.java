package com.company.skillplatform.post.dto;

import com.company.skillplatform.session.dto.SessionMiniResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDraftResponse(
        UUID id,
        String status,
        String title,
        String body,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        List<PostAttachmentResponse> attachments,
        SessionMiniResponse session
) {}