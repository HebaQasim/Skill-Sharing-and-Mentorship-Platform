package com.company.skillplatform.notification.service.impl;

import com.company.skillplatform.common.cache.CacheInvalidator;
import com.company.skillplatform.common.cache.CacheKeys;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.notification.cursor.NotificationCursor;
import com.company.skillplatform.notification.cursor.NotificationCursorCodec;
import com.company.skillplatform.notification.dto.NotificationResponse;
import com.company.skillplatform.notification.dto.UnreadCountResponse;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.notification.service.MyNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MyNotificationServiceImpl implements MyNotificationService {

    private static final String TOP_LISTS_CACHE = "top_lists";

    private final NotificationRepository repository;
    private final CacheInvalidator cacheInvalidator;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationResponse> myNotifications(UUID userId, Integer limit, String cursor) {

        int size = (limit == null) ? 20 : Math.min(Math.max(limit, 1), 50);
        boolean firstPage = (cursor == null || cursor.isBlank());

        // Cache only the most common request: first page with size=20
        if (firstPage && size == 20) {
            return myNotificationsTop20Cached(userId);
        }

        return fetchUncached(userId, size, cursor);
    }

    @Cacheable(cacheNames = TOP_LISTS_CACHE,
            key = "T(com.company.skillplatform.common.cache.CacheKeys).notificationsTop(#userId)")
    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationResponse> myNotificationsTop20Cached(UUID userId) {
        return fetchUncached(userId, 20, null);
    }

    @Override
    public void markRead(UUID userId, UUID notificationId) {
        Notification n = loadOwned(userId, notificationId);
        n.markRead();

        // invalidate cached top 20 because read/unread affects UI color
        cacheInvalidator.evict(TOP_LISTS_CACHE, CacheKeys.notificationsTop(userId));
        cacheInvalidator.evict(TOP_LISTS_CACHE, CacheKeys.notificationsUnreadCount(userId));
    }

    @Override
    public void markUnread(UUID userId, UUID notificationId) {
        Notification n = loadOwned(userId, notificationId);
        n.markUnread();

        cacheInvalidator.evict(TOP_LISTS_CACHE, CacheKeys.notificationsTop(userId));
        cacheInvalidator.evict(TOP_LISTS_CACHE, CacheKeys.notificationsUnreadCount(userId));
    }
    @Cacheable(
            cacheNames = TOP_LISTS_CACHE,
            key = "T(com.company.skillplatform.common.cache.CacheKeys).notificationsUnreadCount(#userId)"
    )
    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(UUID userId) {
        long count = repository.countByRecipientUserIdAndReadAtIsNull(userId);
        return new UnreadCountResponse(count);
    }
    private Notification loadOwned(UUID userId, UUID notificationId) {
        Notification n = repository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.NOTIFICATION_NOT_FOUND, "Notification not found"
                ));

        if (!n.getRecipientUserId().equals(userId)) {

            throw new ResourceNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, "Notification not found");
        }
        return n;
    }

    private CursorPageResponse<NotificationResponse> fetchUncached(UUID userId, int size, String cursor) {

        var pageable = PageRequest.of(0, size);
        List<Notification> items;

        if (cursor == null || cursor.isBlank()) {
            items = repository.findByRecipientUserIdOrderByCreatedAtDescIdDesc(userId, pageable);
        } else {
            NotificationCursor c = NotificationCursorCodec.decode(cursor);
            items = repository.findNextPage(userId, c.createdAt(), c.id(), pageable);
        }

        List<NotificationResponse> mapped = items.stream().map(this::toResponse).toList();

        String nextCursor = null;
        if (items.size() == size) {
            Notification last = items.get(items.size() - 1);
            nextCursor = NotificationCursorCodec.encode(new NotificationCursor(last.getCreatedAt(), last.getId()));
        }

        return new CursorPageResponse<>(mapped, nextCursor);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.getLink(),
                n.getCreatedAt(),
                n.getReadAt(),
                n.isUnread()
        );
    }
}
