package com.company.skillplatform.admin.audit.service;

import com.company.skillplatform.admin.audit.enums.AdminAuditAction;
import com.company.skillplatform.admin.audit.enums.AdminAuditTargetType;

import java.util.UUID;

public interface AdminAuditService {

    void log(UUID moderatorUserId,
             AdminAuditAction action,
             AdminAuditTargetType targetType,
             UUID targetId,
             UUID contextId,
             String note,
             String ip,
             String userAgent);
}
