package com.company.skillplatform.mentor.service.impl;

import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.mentor.dto.DecideMentorRequest;
import com.company.skillplatform.mentor.entity.*;
import com.company.skillplatform.mentor.enums.MentorRequestStatus;
import com.company.skillplatform.mentor.repository.MentorBadgeRequestRepository;
import com.company.skillplatform.mentor.service.MentorBadgeAdminService;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MentorBadgeAdminServiceImpl implements MentorBadgeAdminService {

    private final MentorBadgeRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public void approve(UUID adminId, UUID requestId, DecideMentorRequest req) {
        MentorBadgeRequest r = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_FOUND, "Mentor request not found"));

        if (r.getStatus() != MentorRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "Request is not pending");
        }

        User user = userRepository.findById(r.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (!user.isMentor()) {
            user.grantMentor();
            userRepository.save(user);
        }

        r.approve(adminId, req.note());
        requestRepository.save(r);

        notificationRepository.save(Notification.builder()
                .recipientUserId(user.getId())
                .type(NotificationType.MENTOR_BADGE_APPROVED)
                .title("Mentor badge approved")
                .message("Congratulations! You earned the Mentor badge.")
                .link("/users/" + user.getId())
                .build());
    }

    @Override
    public void reject(UUID adminId, UUID requestId, DecideMentorRequest req) {
        MentorBadgeRequest r = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_FOUND, "Mentor request not found"));

        if (r.getStatus() != MentorRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "Request is not pending");
        }

        r.reject(adminId, req.note());
        requestRepository.save(r);

        notificationRepository.save(Notification.builder()
                .recipientUserId(r.getUserId())
                .type(NotificationType.MENTOR_BADGE_REJECTED)
                .title("Mentor badge not granted yet")
                .message("Your Mentor badge request was rejected. Keep hosting sessions — we'll review again after 5 more valid sessions.")
                .link("/me/sessions")
                .build());
    }
}
