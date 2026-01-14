package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.session.dto.SessionResponse;
import com.company.skillplatform.session.dto.UpsertDraftSessionRequest;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.session.service.PostDraftSessionService;
import com.company.skillplatform.session.service.SessionConflictService;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostDraftSessionServiceImpl implements PostDraftSessionService {

    private final PostRepository postRepository;
    private final SessionRepository sessionRepository;
    private final SessionConflictService conflictService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;


    @Override
    public SessionResponse upsert(UUID userId, UUID postId, UpsertDraftSessionRequest request) {
        Post post = mustBeOwnerDraft(userId, postId);

        String title = sanitizeTitle(request.title());
        String link = sanitizeLink(request.meetingLink());


        if (request.startsAt() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "startsAt is required");
        }
        if (request.durationMinutes() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "durationMinutes must be positive");
        }
        var startsAt = request.startsAt();
        var endAt = startsAt.plusMinutes(request.durationMinutes());


        UUID excludeSessionId = sessionRepository.findByPostId(post.getId()).map(Session::getId).orElse(null);


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

            notificationRepository.save(Notification.builder()
                    .recipientUserId(userId)
                    .type(com.company.skillplatform.notification.enums.NotificationType.SESSION_DEPARTMENT_CONFLICT_WARNING)
                    .title("Department session overlap warning")
                    .message("There are overlapping sessions in your department: " + list)
                    .link("/posts/" + postId)
                    .build());
        }


        Session session = sessionRepository.findByPostId(post.getId())
                .orElseGet(() -> Session.builder()
                        .postId(post.getId())
                        .hostUserId(userId)
                        .status(SessionStatus.DRAFT)
                        .title(title)
                        .startsAt(request.startsAt())
                        .durationMinutes(request.durationMinutes())
                        .meetingLink(link)
                        .build());


        session.updateDetails(title, request.startsAt(), request.durationMinutes(), link);

        Session saved = sessionRepository.save(session);
        return toResponse(saved);
    }

    @Override
    public void remove(UUID userId, UUID postId) {
        mustBeOwnerDraft(userId, postId);
        sessionRepository.deleteByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SessionResponse> get(UUID userId, UUID postId) {
        mustBeOwnerDraft(userId, postId);
        return sessionRepository.findByPostId(postId).map(this::toResponse);
    }

    private Post mustBeOwnerDraft(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) throw new ForbiddenException("Not allowed");
        if (post.getStatus() != PostStatus.DRAFT) {
            throw new BusinessException(ErrorCode.CONFLICT, "it is not in draft ");
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
        if (v == null) return null;
        String t = v.trim().replaceAll("\\s{2,}", " ");
        if (t.isBlank()) throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Session title is required");
        return t;
    }

    private String sanitizeLink(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isBlank() ? null : t;
    }
}
