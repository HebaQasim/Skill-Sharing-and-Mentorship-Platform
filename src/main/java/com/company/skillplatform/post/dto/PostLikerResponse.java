package com.company.skillplatform.post.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostLikerResponse(
        UUID userId,
        String fullName,
        String department,
        String jobTitle,
        String profileImageUrl,
        LocalDateTime likedAt
) {}

