package com.company.skillplatform.post.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostCardCached(
        UUID id,
        String title,
        String bodyPreview,
        LocalDateTime publishedAt,
        PostCardResponse.AuthorCard author,
        long likesCount,
        long commentsCount,
        List<PostCardResponse.AttachmentMini> attachments,
        PostCardResponse.SessionMini session
) {}
