package com.company.skillplatform.session.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionPersonRow(
        UUID userId,
        String fullName,
        String department,
        String jobTitle,
        LocalDateTime joinedAt
) {}
