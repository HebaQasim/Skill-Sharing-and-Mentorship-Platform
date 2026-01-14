package com.company.skillplatform.session.listener;

import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.event.SessionBecameLiveEvent;
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
public class SessionBecameLiveListener {

    private final SessionRepository sessionRepository;
    private final SessionRegistrationRepository registrationRepository;
    private final NotificationRepository notificationRepository;

 @Transactional
 @EventListener
    public void onSessionLive(SessionBecameLiveEvent event) {

        Session s = sessionRepository.findById(event.sessionId()).orElse(null);
        if (s == null) return;

        List<UUID> recipients = registrationRepository.findRegistrantUserIds(s.getId());
        if (recipients.isEmpty()) return;


        NotificationType type = NotificationType.SESSION_LIVE;

        List<Notification> notifications = recipients.stream()
                .map(userId -> Notification.builder()
                        .recipientUserId(userId)
                        .type(type)
                        .title("Session is live now")
                        .message("Your session \"" + s.getTitle() + "\" is live now. You can join.")
                        .link("/sessions/" + s.getId())
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }
}
