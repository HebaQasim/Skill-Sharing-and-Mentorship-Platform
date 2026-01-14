package com.company.skillplatform.notification.cursor;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationCursor(LocalDateTime createdAt, UUID id) {}

