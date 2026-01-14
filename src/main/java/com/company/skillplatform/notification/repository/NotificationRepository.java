package com.company.skillplatform.notification.repository;

import com.company.skillplatform.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findTop20ByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
    // First page
    List<Notification> findByRecipientUserIdOrderByCreatedAtDescIdDesc(UUID recipientUserId, Pageable pageable);

    // Next pages
    @Query("""
        select n
        from Notification n
        where n.recipientUserId = :userId
          and (
               n.createdAt < :cursorCreatedAt
               or (n.createdAt = :cursorCreatedAt and n.id < :cursorId)
          )
        order by n.createdAt desc, n.id desc
    """)
    List<Notification> findNextPage(
            @Param("userId") UUID userId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") UUID cursorId,
            Pageable pageable
    );
    long countByRecipientUserIdAndReadAtIsNull(UUID recipientUserId);

}
