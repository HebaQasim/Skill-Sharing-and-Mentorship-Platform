package com.company.skillplatform.notification.dto;

import com.company.skillplatform.notification.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String title,
        String message,
        String link,
        LocalDateTime createdAt,
        LocalDateTime readAt,
        boolean unread
) {}

