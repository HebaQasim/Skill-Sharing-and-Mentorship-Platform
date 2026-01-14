package com.company.skillplatform.notification.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient", columnList = "recipientUserId"),
                @Index(name = "idx_notifications_read", columnList = "readAt"),
                @Index(name="idx_notifications_recipient_created", columnList = "recipientUserId,createdAt,id"),
                @Index(name="idx_notifications_recipient_read", columnList = "recipientUserId,readAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Column(nullable = false)
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private NotificationType type;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 200)
    private String link; //  "/admin/requests/{id}", "/me/requests"

    private LocalDateTime readAt;

    public boolean isUnread() {
        return readAt == null;
    }

    public void markRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public void markUnread() {
        this.readAt = null;
    }
}

