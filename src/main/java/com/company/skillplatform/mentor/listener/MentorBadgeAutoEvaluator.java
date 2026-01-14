package com.company.skillplatform.mentor.listener;

import com.company.skillplatform.mentor.entity.*;
import com.company.skillplatform.mentor.enums.MentorRequestStatus;
import com.company.skillplatform.mentor.repository.MentorBadgeRequestRepository;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.event.SessionCompletedEvent;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MentorBadgeAutoEvaluator {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final MentorBadgeRequestRepository mentorRequestRepository;
    private final NotificationRepository notificationRepository;

    @EventListener
    @Transactional
    public void onSessionCompleted(SessionCompletedEvent event) {

        User host = userRepository.findById(event.hostUserId()).orElse(null);
        if (host == null) return;

        if (host.isMentor()) return;


        if (mentorRequestRepository.existsByUserIdAndStatus(host.getId(), MentorRequestStatus.PENDING)) {
            return;
        }

        long validCount = sessionRepository.countByHostAndStatus(host.getId(), SessionStatus.COMPLETED);


        Optional<MentorBadgeRequest> lastOpt = mentorRequestRepository.findTopByUserIdOrderByCreatedAtDesc(host.getId());

        long nextThreshold = computeNextThreshold(validCount, lastOpt);

        if (nextThreshold == -1) return;

        MentorBadgeRequest req = MentorBadgeRequest.builder()
                .id(UUID.randomUUID())
                .userId(host.getId())
                .thresholdCount(nextThreshold)
                .status(MentorRequestStatus.PENDING)
                .build();

        mentorRequestRepository.save(req);


        List<User> admins = chooseAdmins(host.getDepartment());
        if (admins.isEmpty()) return;

        List<Notification> notifs = new ArrayList<>();
        for (User admin : admins) {
            notifs.add(Notification.builder()
                    .recipientUserId(admin.getId())
                    .type(NotificationType.MENTOR_BADGE_REVIEW_REQUESTED)
                    .title("Mentor badge review")
                    .message("User '" + host.getFullName() + "' reached " + nextThreshold +
                            " valid sessions. Please review sessions & feedback and approve/reject.")
                    .link("/admin/mentor-requests/" + req.getId())
                    .build());
        }
        notificationRepository.saveAll(notifs);
    }

    private long computeNextThreshold(long validCount, Optional<MentorBadgeRequest> lastOpt) {

        if (validCount < 5) return -1;


        if (lastOpt.isEmpty()) {
            return (validCount >= 5 && validCount % 5 == 0) ? validCount : -1;
        }

        MentorBadgeRequest last = lastOpt.get();


        if (last.getStatus() == MentorRequestStatus.APPROVED) return -1;


        if (last.getStatus() == MentorRequestStatus.REJECTED) {
            long needed = last.getThresholdCount() + 5;
            return (validCount >= needed && validCount % 5 == 0) ? validCount : -1;
        }

        return -1;
    }

    private List<User> chooseAdmins(String department) {
        List<User> deptAdmins = userRepository.findDeptAdmins(department);
        if (!deptAdmins.isEmpty()) return deptAdmins;
        return userRepository.findGlobalAdmins();
    }
}
