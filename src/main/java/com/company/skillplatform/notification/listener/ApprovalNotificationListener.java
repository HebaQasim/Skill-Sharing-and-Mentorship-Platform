package com.company.skillplatform.notification.listener;

import com.company.skillplatform.approval.event.ApprovalRequestedEvent;
import com.company.skillplatform.approval.repository.ApprovalRequestRepository;
import com.company.skillplatform.common.cache.CacheInvalidator;
import com.company.skillplatform.common.cache.CacheKeys;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ApprovalNotificationListener {
    private static final String TOP_LISTS_CACHE = "top_lists";
    private final NotificationRepository notificationRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final CacheInvalidator cacheInvalidator;

    @EventListener
    @Transactional
    public void onApprovalRequested(ApprovalRequestedEvent event) {
        var req = approvalRequestRepository.findById(event.requestId()).orElseThrow();

        notificationRepository.save(Notification.builder()
                .recipientUserId(req.getAssignedToUserId())
                .type(NotificationType.APPROVAL_REQUESTED)
                .title("New approval request")
                .message("You have a new request to review.")
                .link("/admin/approvals/" + req.getId())
                .build()
        );
        cacheInvalidator.evict(TOP_LISTS_CACHE, CacheKeys.notificationsTop(req.getAssignedToUserId()));
        cacheInvalidator.evict(TOP_LISTS_CACHE, CacheKeys.notificationsUnreadCount(req.getRequestedByUserId()));
    }
}

