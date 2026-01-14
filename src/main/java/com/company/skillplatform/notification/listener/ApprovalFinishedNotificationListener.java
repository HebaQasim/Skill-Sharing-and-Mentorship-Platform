package com.company.skillplatform.notification.listener;

import com.company.skillplatform.approval.enums.ApprovalStatus;
import com.company.skillplatform.approval.event.ApprovalFinishedEvent;
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
public class ApprovalFinishedNotificationListener {
    private static final String TOP_LISTS_CACHE = "top_lists";
    private final ApprovalRequestRepository approvalRequestRepository;
    private final NotificationRepository notificationRepository;
private final CacheInvalidator cacheInvalidator;
    @EventListener
    @Transactional
    public void onDecision(ApprovalFinishedEvent event) {
        var req = approvalRequestRepository.findById(event.requestId()).orElseThrow();

        boolean approved = event.status() == ApprovalStatus.APPROVED;

        notificationRepository.save(Notification.builder()
                .recipientUserId(req.getRequestedByUserId())
                .type(approved ? NotificationType.APPROVAL_APPROVED : NotificationType.APPROVAL_REJECTED)
                .title(approved ? "Request approved" : "Request rejected")
                .message(approved
                        ? "Your request has been approved."
                        : "Your request has been rejected.")
                .link("/me/approval-requests")
                .build()
        );
        cacheInvalidator.evict(TOP_LISTS_CACHE, CacheKeys.notificationsTop(req.getRequestedByUserId()));
        cacheInvalidator.evict(TOP_LISTS_CACHE, CacheKeys.notificationsUnreadCount(req.getRequestedByUserId()));
    }
}
