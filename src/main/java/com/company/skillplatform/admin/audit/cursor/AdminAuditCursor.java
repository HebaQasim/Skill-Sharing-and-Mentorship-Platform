package com.company.skillplatform.admin.audit.cursor;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminAuditCursor(LocalDateTime createdAt, UUID id) {}
