package com.company.skillplatform.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDetailsResponse(
        UUID id,
        String title,
        String body,
        LocalDateTime publishedAt,
        LocalDateTime editedAt,
        long likesCount,
        boolean likedByMe,
        long commentCount,
        PostCardResponse.AuthorCard author,
        List<AttachmentFull> attachments,
        SessionFull session,
        long feedbackCount
) {
    public record AttachmentFull(
            UUID id,
            String type,
            String originalFilename,
            String contentType,
            long sizeBytes,
            String url,
            LocalDateTime createdAt
    ) {}

    public record SessionFull(
            UUID id,
            String title,
            LocalDateTime startsAt,
            int durationMinutes,
            String meetingLink,
            String status

    ) {}
}
