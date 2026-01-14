package com.company.skillplatform.session.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionRegistrationResponse(
        UUID sessionId,
        UUID userId,
        LocalDateTime registeredAt
) {}
