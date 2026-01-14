package com.company.skillplatform.session.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionMiniResponse(
        UUID id,
        String title,
        LocalDateTime startsAt,
        int durationMinutes,
        String meetingLink,
        String status
) {}

