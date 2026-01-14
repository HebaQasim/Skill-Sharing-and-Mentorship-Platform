package com.company.skillplatform.session.listener;

import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.event.SessionUpdatedEvent;
import com.company.skillplatform.session.repository.SessionRegistrationRepository;
import com.company.skillplatform.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionUpdatedListener {

    private final SessionRepository sessionRepository;
    private final SessionRegistrationRepository registrationRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    @EventListener
    public void onUpdated(SessionUpdatedEvent event) {

        Session s = sessionRepository.findById(event.sessionId()).orElse(null);
        if (s == null) return;

        List<UUID> recipients = registrationRepository.findRegistrantUserIds(s.getId());
        if (recipients.isEmpty()) return;

        List<Notification> list = recipients.stream()
                .map(uid -> Notification.builder()
                        .recipientUserId(uid)
                        .type(NotificationType.SESSION_UPDATED)
                        .title("Session updated")
                        .message("Session \"" + s.getTitle() + "\" was updated. Please check the new details.")
                        .link("/posts/" + event.postId())
                        .build())
                .toList();

        notificationRepository.saveAll(list);
    }
}

