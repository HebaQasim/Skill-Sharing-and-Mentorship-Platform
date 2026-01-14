package com.company.skillplatform.session.job;

import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.event.SessionBecameLiveEvent;
import com.company.skillplatform.session.event.SessionCompletedEvent;
import com.company.skillplatform.session.repository.SessionAttendanceRepository;
import com.company.skillplatform.session.repository.SessionRegistrationRepository;
import com.company.skillplatform.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SessionStatusAutoUpdater {

    private final SessionRepository sessionRepository;
    private final SessionRegistrationRepository registrationRepository;

    private final ApplicationEventPublisher publisher;
    private final SessionAttendanceRepository attendanceRepository;

    private final NotificationRepository notificationRepository;

    /**
     * Runs every 1 minute
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void updateEndedSessions() {

        LocalDateTime now = LocalDateTime.now();

        List<Session> toLive = sessionRepository.findToGoLive(SessionStatus.SCHEDULED, now, now.plusMinutes(10));
        for (Session s : toLive) {
            s.markLive();
            publisher.publishEvent(new SessionBecameLiveEvent(s.getId()));
        }



        List<Session> ended = sessionRepository.findEndedLive(SessionStatus.LIVE, now);
        if (ended.isEmpty()) return;

        List<UUID> ids = ended.stream().map(Session::getId).toList();

        // Batch counts
        Map<UUID, Long> attendanceCounts =
                attendanceRepository.countAttendanceBySessionIds(ids).stream()
                        .collect(Collectors.toMap(
                                SessionAttendanceRepository.SessionAttendanceCountRow::getSessionId,
                                SessionAttendanceRepository.SessionAttendanceCountRow::getCnt
                        ));

        // Batch attendees
        Map<UUID, List<UUID>> attendeesBySession = attendanceRepository.attendeesBySessionIds(ids).stream()
                .collect(Collectors.groupingBy(
                        SessionAttendanceRepository.SessionAttendeeRow::getSessionId,
                        Collectors.mapping(SessionAttendanceRepository.SessionAttendeeRow::getUserId, Collectors.toList())
                ));


        List<Notification> notifications = new ArrayList<>();

        for (Session s : ended) {
            long attendedCount = attendanceCounts.getOrDefault(s.getId(), 0L);


            if (attendedCount >= 1) {
                s.markCompleted();
                publisher.publishEvent(new SessionCompletedEvent(s.getId(), s.getHostUserId()));
                notifications.add(Notification.builder()
                        .recipientUserId(s.getHostUserId())
                        .type(NotificationType.SESSION_COMPLETED)
                        .title("Session completed")
                        .message("Your session '" + s.getTitle() + "' is completed (" + attendedCount + " attendee(s)).")
                        .link("/posts/" + s.getPostId())
                        .build());

                List<UUID> attendeeIds = attendeesBySession.getOrDefault(s.getId(), List.of());

                for (UUID attendeeId : attendeeIds) {

                    if (attendeeId.equals(s.getHostUserId())) continue;

                    notifications.add(Notification.builder()
                            .recipientUserId(attendeeId)
                            .type(NotificationType.SESSION_FEEDBACK_REQUEST)
                            .title("Feedback available")
                            .message("You attended '" + s.getTitle() + "'. You can now leave feedback.")
                            .link("/sessions/" + s.getId() + "/feedback")
                            .build());
                }


            } else {
                s.markUnattended();

                notifications.add(Notification.builder()
                        .recipientUserId(s.getHostUserId())
                        .type(NotificationType.SESSION_UNATTENDED)
                        .title("Session unattended")
                        .message("Your session '" + s.getTitle() + "' ended with no attendees.")
                        .link("/posts/" + s.getPostId())
                        .build());
            }
        }


        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }
}

