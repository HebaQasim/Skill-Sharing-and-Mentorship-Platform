package com.company.skillplatform.admin.audit.service.impl;

import com.company.skillplatform.admin.audit.entity.*;
import com.company.skillplatform.admin.audit.enums.AdminAuditAction;
import com.company.skillplatform.admin.audit.enums.AdminAuditTargetType;
import com.company.skillplatform.admin.audit.repository.AdminAuditLogRepository;
import com.company.skillplatform.admin.audit.service.AdminAuditService;
import com.company.skillplatform.common.cache.CacheStampService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAuditServiceImpl implements AdminAuditService {

    private final AdminAuditLogRepository repo;
    private final CacheStampService cacheStampService;

    @Override
    public void log(UUID moderatorUserId,
                    AdminAuditAction action,
                    AdminAuditTargetType targetType,
                    UUID targetId,
                    UUID contextId,
                    String note,
                    String ip,
                    String userAgent) {

        repo.save(AdminAuditLog.builder()
                .moderatorUserId(moderatorUserId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .contextId(contextId)
                .note(note)
                .ip(ip)
                .userAgent(userAgent)
                .build());

        cacheStampService.bump(AdminAuditQueryServiceImpl.AUDIT_STAMP_NAME);
    }
}
