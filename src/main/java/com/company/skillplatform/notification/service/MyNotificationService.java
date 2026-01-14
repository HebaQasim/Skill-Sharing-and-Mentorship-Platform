package com.company.skillplatform.notification.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.notification.dto.NotificationResponse;
import com.company.skillplatform.notification.dto.UnreadCountResponse;

import java.util.UUID;

public interface MyNotificationService {

    CursorPageResponse<NotificationResponse> myNotifications(UUID userId, Integer limit, String cursor);

    void markRead(UUID userId, UUID notificationId);

    void markUnread(UUID userId, UUID notificationId);
    UnreadCountResponse unreadCount(UUID userId);

}

