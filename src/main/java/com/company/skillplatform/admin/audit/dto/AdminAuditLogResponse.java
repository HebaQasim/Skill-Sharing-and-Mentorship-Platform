package com.company.skillplatform.admin.audit.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminAuditLogResponse(
        UUID id,
        UUID moderatorUserId,
        String action,
        String targetType,
        UUID targetId,
        UUID contextId,
        String note,
        String ip,
        String userAgent,
        LocalDateTime createdAt
) {}
