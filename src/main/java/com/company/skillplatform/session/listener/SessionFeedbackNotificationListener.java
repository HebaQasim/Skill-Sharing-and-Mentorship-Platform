package com.company.skillplatform.session.listener;

import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.event.SessionFeedbackAddedEvent;
import com.company.skillplatform.session.event.SessionFeedbackUpdatedEvent;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionFeedbackNotificationListener {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Async
    @EventListener
    @Transactional
    public void onFeedbackAdded(SessionFeedbackAddedEvent event) {
        sendToHost(event.sessionId(), event.authorUserId(), true);
    }

    @Async
    @EventListener
    @Transactional
    public void onFeedbackUpdated(SessionFeedbackUpdatedEvent event) {
        sendToHost(event.sessionId(), event.authorUserId(), false);
    }

    private void sendToHost(UUID sessionId, UUID authorUserId, boolean added) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SESSION_NOT_FOUND, "Session not found"));

        if (s.getHostUserId().equals(authorUserId)) return;

        User user = userRepository.findById(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String title = added ? "New feedback" : "Feedback updated";
        String msg = added
                ? (user.getFullName() + " added feedback to your session.")
                : (user.getFullName() + " updated feedback on your session.");

        NotificationType type = added
                ? NotificationType.SESSION_FEEDBACK_ADDED
                : NotificationType.SESSION_FEEDBACK_UPDATED;

        notificationRepository.save(Notification.builder()
                .recipientUserId(s.getHostUserId())
                .type(type)
                .title(title)
                .message(msg)
                .link("/posts/" + s.getPostId())
                .build());
    }
}
