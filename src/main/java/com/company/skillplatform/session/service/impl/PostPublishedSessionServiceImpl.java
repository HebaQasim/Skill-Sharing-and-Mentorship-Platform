package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.session.dto.SessionResponse;
import com.company.skillplatform.session.dto.UpdatePublishedSessionRequest;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.event.SessionCanceledEvent;
import com.company.skillplatform.session.event.SessionUpdatedEvent;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.session.service.PostPublishedSessionService;
import com.company.skillplatform.session.service.SessionConflictService;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostPublishedSessionServiceImpl implements PostPublishedSessionService {

    public static final String FEED_STAMP_NAME = "postFeed";

    private final PostRepository postRepository;
    private final SessionRepository sessionRepository;
    private final CacheStampService cacheStampService;
    private final ApplicationEventPublisher publisher;
    private final SessionConflictService conflictService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;




    @Override
    public SessionResponse upsertOrUpdate(UUID userId, UUID postId, UpdatePublishedSessionRequest request) {
        Post post = mustBeOwnerPublished(userId, postId);



        Session session = sessionRepository.findByPostId(postId)
                .orElseGet(() -> Session.builder()
                        .postId(postId)
                        .hostUserId(userId)
                        .status(SessionStatus.SCHEDULED)
                        .build());


        if (session.getStatus() == SessionStatus.CANCELED) {
            session.scheduleOnPublish();
        }
        if (request.startsAt() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "startsAt is required");
        }
        if (request.durationMinutes() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "durationMinutes must be positive");
        }

        var startsAt = request.startsAt();
        var endAt = startsAt.plusMinutes(request.durationMinutes());

        UUID excludeSessionId = sessionRepository.findByPostId(postId).map(Session::getId).orElse(null);


        conflictService.assertHostNoBlockingConflicts(userId, startsAt, endAt, excludeSessionId);


        String dept = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"))
                .getDepartment();

        var deptConflicts = conflictService.findDepartmentConflictsForWarning(dept, startsAt, endAt, excludeSessionId);

        if (!deptConflicts.isEmpty()) {
            String list = deptConflicts.stream()
                    .map(c -> c.getTitle() + " (" + c.getStartsAt() + ")")
                    .limit(5)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            notificationRepository.save(com.company.skillplatform.notification.entity.Notification.builder()
                    .recipientUserId(userId)
                    .type(com.company.skillplatform.notification.enums.NotificationType.SESSION_DEPARTMENT_CONFLICT_WARNING)
                    .title("Department session overlap warning")
                    .message("There are overlapping sessions in your department: " + list)
                    .link("/posts/" + postId)
                    .build());
        }

        session.updateDetails(
                sanitizeTitle(request.title()),
                request.startsAt(),
                request.durationMinutes(),
                sanitizeLink(request.meetingLink())
        );

        Session saved = sessionRepository.save(session);

        publisher.publishEvent(new SessionUpdatedEvent(saved.getId(), postId));

        cacheStampService.bump(FEED_STAMP_NAME);

        return toResponse(saved);
    }

    @Override
    public void cancel(UUID userId, UUID postId) {
        mustBeOwnerPublished(userId, postId);

        Session session = sessionRepository.findByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SESSION_NOT_FOUND, "Session not found"));


        if (session.getStartsAt() != null && !session.getStartsAt().isAfter(java.time.LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CONFLICT, "You can't cancel a session after it starts");
        }


        if (session.getStatus() == SessionStatus.COMPLETED || session.getStatus() == SessionStatus.UNATTENDED) {
            throw new BusinessException(ErrorCode.CONFLICT, "You can't cancel a finished session");
        }

        if (session.getStatus() != SessionStatus.CANCELED) {
            session.cancel();
            cacheStampService.bump(FEED_STAMP_NAME);


            publisher.publishEvent(new SessionCanceledEvent(session.getId(), postId));
        }
    }



    private Post mustBeOwnerPublished(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) throw new ForbiddenException("Not allowed");
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Post must be published");
        }
        return post;
    }

    private SessionResponse toResponse(Session s) {
        return new SessionResponse(
                s.getId(),
                s.getPostId(),
                s.getTitle(),
                s.getStartsAt(),
                s.getDurationMinutes(),
                s.getMeetingLink(),
                s.getStatus().name()
        );
    }

    private String sanitizeTitle(String v) {
        String t = v == null ? "" : v.trim().replaceAll("\\s{2,}", " ");
        if (t.isBlank()) throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Session title is required");
        return t;
    }

    private String sanitizeLink(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isBlank() ? null : t;
    }
}
