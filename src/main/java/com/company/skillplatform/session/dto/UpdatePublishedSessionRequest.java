package com.company.skillplatform.session.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record UpdatePublishedSessionRequest(

        @NotBlank(message = "Session title is required")
        @Size(max = 160, message = "Session title must not exceed 160 characters")
        String title,

        @NotNull
        @Future(message = "Session start time must be in the future")
        LocalDateTime startsAt,

        @Min(value = 15, message = "Duration must be at least 15 minutes")
        @Max(value = 480, message = "Duration must not exceed 480 minutes")
        int durationMinutes,

        @Size(max = 400, message = "Meeting link must not exceed 400 characters")
        String meetingLink
) {}
