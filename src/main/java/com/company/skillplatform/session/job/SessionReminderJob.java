package com.company.skillplatform.session.job;

import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.repository.SessionRegistrationRepository;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionReminderJob {

    private static final EnumSet<SessionStatus> ACTIVE = EnumSet.of(SessionStatus.SCHEDULED, SessionStatus.LIVE);

    private final SessionRepository sessionRepository;
    private final SessionRegistrationRepository registrationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;


    // runs every minute
    @Scheduled(cron = "0 * * * * *")
    public void run() {
        sendHost24hReminder();
        sendRegistrants1hReminder();
    }

    private void sendHost24hReminder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusHours(24);
        LocalDateTime to = from.plusMinutes(1);

        var sessions = sessionRepository.findHostReminderDue(from, to, ACTIVE);

        for (Session s : sessions) {
            // In-app
            notificationRepository.save(Notification.builder()
                    .recipientUserId(s.getHostUserId())
                    .type(NotificationType.SESSION_REMINDER_HOST_24H)
                    .title("Session reminder")
                    .message("Your session \"" + s.getTitle() + "\" starts in 24 hours.")
                    .link("/posts/" + s.getPostId())
                    .build());


        }
    }

    private void sendRegistrants1hReminder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.plusHours(1);
        LocalDateTime to = from.plusMinutes(1);

        var sessions = sessionRepository.findRegistrantsReminderDue(from, to, ACTIVE);

        for (Session s : sessions) {
            // In-app to all registrants
            var userIds = registrationRepository.findRegistrantUserIds(s.getId());
            if (!userIds.isEmpty()) {
                var notifs = userIds.stream()
                        .map(uid -> Notification.builder()
                                .recipientUserId(uid)
                                .type(NotificationType.SESSION_REMINDER_ATTENDEE_1H) // add enum value
                                .title("Session starts soon")
                                .message("Session \"" + s.getTitle() + "\" starts in 1 hour.")
                                .link("/posts/" + s.getPostId())
                                .build())
                        .toList();
                notificationRepository.saveAll(notifs);
            }


        }
    }
}
