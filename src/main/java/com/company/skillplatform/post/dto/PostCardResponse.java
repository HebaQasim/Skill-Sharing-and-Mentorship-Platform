package com.company.skillplatform.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostCardResponse(
        UUID id,
        String title,
        String bodyPreview,
        LocalDateTime publishedAt,

        AuthorCard author,
        long likesCount,
        boolean likedByMe,
        long commentCount,
        List<AttachmentMini> attachments,
        SessionMini session
) {
    public record AuthorCard(
            UUID id,
            String fullName,
            String department,
            String jobTitle,
            String headline,
            String profileImageUrl

    ) {}

    public record AttachmentMini(
            UUID id,
            String type,
            String url
    ) {}

    public record SessionMini(
            UUID id,
            String title,
            LocalDateTime startsAt,
            int durationMinutes,
            String status

    ) {}
}
