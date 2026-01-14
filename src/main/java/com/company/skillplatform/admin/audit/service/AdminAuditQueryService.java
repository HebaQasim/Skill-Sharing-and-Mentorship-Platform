package com.company.skillplatform.admin.audit.service;

import com.company.skillplatform.admin.audit.dto.AdminAuditLogResponse;
import com.company.skillplatform.common.dto.CursorPageResponse;

public interface AdminAuditQueryService {
    CursorPageResponse<AdminAuditLogResponse> list(Integer limit, String cursor);
}
