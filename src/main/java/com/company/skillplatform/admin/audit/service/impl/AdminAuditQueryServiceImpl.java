package com.company.skillplatform.admin.audit.service.impl;

import com.company.skillplatform.admin.audit.cursor.AdminAuditCursor;
import com.company.skillplatform.admin.audit.cursor.AdminAuditCursorCodec;
import com.company.skillplatform.admin.audit.dto.AdminAuditLogResponse;
import com.company.skillplatform.admin.audit.entity.AdminAuditLog;
import com.company.skillplatform.admin.audit.repository.AdminAuditLogRepository;
import com.company.skillplatform.admin.audit.service.AdminAuditQueryService;
import com.company.skillplatform.common.cache.CacheKeys;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAuditQueryServiceImpl implements AdminAuditQueryService {

    public static final String AUDIT_STAMP_NAME = "adminAudit";
    public static final String AUDIT_CACHE_NAME = "adminAuditCache";

    private final AdminAuditLogRepository repo;
    private final CacheStampService cacheStampService;

    @Override
    public CursorPageResponse<AdminAuditLogResponse> list(Integer limit, String cursor) {

        int size = normalize(limit);
        boolean firstPage = (cursor == null || cursor.isBlank());


        if (firstPage && size == 20) {
            long stamp = cacheStampService.getStamp(AUDIT_STAMP_NAME);
            return top20Cached(stamp);
        }

        return fetchUncached(size, cursor);
    }

    @Cacheable(
            cacheNames = AUDIT_CACHE_NAME,
            key = "T(com.company.skillplatform.common.cache.CacheKeys).adminAuditTop20(#stamp)"
    )
    public CursorPageResponse<AdminAuditLogResponse> top20Cached(long stamp) {
        return fetchUncached(20, null);
    }

    private CursorPageResponse<AdminAuditLogResponse> fetchUncached(int size, String cursor) {

        var pageable = PageRequest.of(0, size + 1);

        List<AdminAuditLog> items;
        if (cursor == null || cursor.isBlank()) {
            items = repo.findByOrderByCreatedAtDescIdDesc(pageable);
        } else {
            AdminAuditCursor c = AdminAuditCursorCodec.decode(cursor);
            items = repo.findNextPage(c.createdAt(), c.id(), pageable);
        }

        boolean hasNext = items.size() > size;
        if (hasNext) items = items.subList(0, size);

        List<AdminAuditLogResponse> mapped = items.stream()
                .map(this::toResponse)
                .toList();

        String nextCursor = null;
        if (hasNext && !items.isEmpty()) {
            AdminAuditLog last = items.get(items.size() - 1);
            nextCursor = AdminAuditCursorCodec.encode(new AdminAuditCursor(last.getCreatedAt(), last.getId()));
        }

        return new CursorPageResponse<>(mapped, nextCursor);
    }

    private AdminAuditLogResponse toResponse(AdminAuditLog a) {
        return new AdminAuditLogResponse(
                a.getId(),
                a.getModeratorUserId(),
                a.getAction().name(),
                a.getTargetType().name(),
                a.getTargetId(),
                a.getContextId(),
                a.getNote(),
                a.getIp(),
                a.getUserAgent(),
                a.getCreatedAt()
        );
    }

    private int normalize(Integer limit) {
        int v = (limit == null) ? 20 : limit;
        v = Math.max(v, 1);
        v = Math.min(v, 50);
        return v;
    }
}
