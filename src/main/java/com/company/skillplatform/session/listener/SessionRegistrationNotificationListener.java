package com.company.skillplatform.session.listener;

import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.event.SessionRegisteredEvent;
import com.company.skillplatform.session.event.SessionRegistrationCanceledEvent;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SessionRegistrationNotificationListener {

    private final NotificationRepository notificationRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void onRegistered(SessionRegisteredEvent e) {
        Session session = sessionRepository.findById(e.sessionId()).orElse(null);
        User attendee = userRepository.findById(e.attendeeUserId()).orElse(null);
        if (session == null || attendee == null) return;

        notificationRepository.save(Notification.builder()
                .recipientUserId(e.hostUserId())
                .type(NotificationType.SESSION_REGISTERED)
                .title("New session registration")
                .message(attendee.getFullName() + " registered for your session: " + session.getTitle())
                .link("/posts/" + session.getPostId())
                .build());
    }

    @EventListener
    @Transactional
    public void onCanceled(SessionRegistrationCanceledEvent e) {
        Session session = sessionRepository.findById(e.sessionId()).orElse(null);
        User attendee = userRepository.findById(e.attendeeUserId()).orElse(null);
        if (session == null || attendee == null) return;

        notificationRepository.save(Notification.builder()
                .recipientUserId(e.hostUserId())
                .type(NotificationType.SESSION_REGISTRATION_CANCELED)
                .title("Registration canceled")
                .message(attendee.getFullName() + " canceled registration for: " + session.getTitle())
                .link("/posts/" + session.getPostId())
                .build());
    }
}
