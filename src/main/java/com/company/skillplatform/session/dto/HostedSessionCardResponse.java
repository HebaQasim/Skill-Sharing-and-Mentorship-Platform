package com.company.skillplatform.session.dto;

import com.company.skillplatform.session.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record HostedSessionCardResponse(
        UUID sessionId,
        UUID postId,
        String title,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Integer durationMinutes,
        String meetingLink,
        String recordingUrl,
        SessionStatus status,
        Long attendanceCount
) {}

