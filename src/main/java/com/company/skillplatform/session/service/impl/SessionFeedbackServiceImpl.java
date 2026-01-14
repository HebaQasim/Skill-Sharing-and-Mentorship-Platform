package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.cursor.*;
import com.company.skillplatform.session.dto.*;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.entity.SessionFeedback;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.event.SessionFeedbackAddedEvent;
import com.company.skillplatform.session.event.SessionFeedbackUpdatedEvent;
import com.company.skillplatform.session.repository.*;
import com.company.skillplatform.session.service.SessionFeedbackService;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionFeedbackServiceImpl implements SessionFeedbackService {

    private final SessionRepository sessionRepository;
    private final SessionFeedbackRepository feedbackRepository;
    private final SessionAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ApplicationEventPublisher publisher;

    @Override
    public FeedbackResponse add(UUID userId, UUID sessionId, CreateFeedbackRequest request) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SESSION_NOT_FOUND, "Session not found"));

        if (s.getStatus() != SessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.CONFLICT, "Feedback is allowed only after session completion");
        }


        feedbackRepository.findBySessionIdAndAuthorUserId(sessionId, userId).ifPresent(existing -> {
            if (!existing.isDeleted()) {
                throw new BusinessException(ErrorCode.CONFLICT, "You already submitted feedback for this session");
            }
        });

        SessionFeedback saved = feedbackRepository.save(SessionFeedback.builder()
                .sessionId(sessionId)
                .authorUserId(userId)
                .rating(request.rating())
                .comment(sanitize(request.comment()))
                .build());

        publisher.publishEvent(new SessionFeedbackAddedEvent(saved.getId(), sessionId, userId));

        return map(saved);
    }

    @Override
    public FeedbackResponse edit(UUID userId, UUID sessionId, UUID feedbackId, EditFeedbackRequest request) {
        SessionFeedback f = feedbackRepository.findByIdAndSessionId(feedbackId, sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, "Feedback not found"));

        if (!f.isOwner(userId)) throw new ForbiddenException("Not allowed");
        if (f.isDeleted()) throw new BusinessException(ErrorCode.CONFLICT, "Feedback is deleted");

        f.edit(request.rating(), sanitize(request.comment()));
        publisher.publishEvent(new SessionFeedbackUpdatedEvent(f.getId(), sessionId, userId));
        return map(f);
    }

    @Override
    public void delete(UUID userId, UUID sessionId, UUID feedbackId) {
        SessionFeedback f = feedbackRepository.findByIdAndSessionId(feedbackId, sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, "Feedback not found"));

        if (!f.isOwner(userId)) throw new ForbiddenException("Not allowed");
        f.softDelete();
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<FeedbackResponse> list(UUID sessionId, Integer limit, String cursor) {
        int size = (limit == null) ? 20 : Math.min(Math.max(limit, 1), 50);
        var pageable = PageRequest.of(0, size);

        var rows = (cursor == null || cursor.isBlank())
                ? feedbackRepository.firstPage(sessionId, pageable)
                : feedbackRepository.nextPage(
                sessionId,
                FeedbackCursorCodec.decode(cursor).createdAt(),
                FeedbackCursorCodec.decode(cursor).id(),
                pageable
        );

        var items = rows.stream().map(r -> {
            boolean deleted = r.getDeletedAt() != null;
            return new FeedbackResponse(
                    r.getId(),
                    r.getSessionId(),
                    r.getRating(),
                    deleted ? null : r.getComment(),
                    r.getCreatedAt(),
                    r.getEditedAt(),
                    deleted,
                    r.getAuthorId(),
                    r.getAuthorFullName(),
                    r.getAuthorDepartment(),
                    r.getAuthorJobTitle(),
                    r.getAuthorHeadline(),
                    r.getAuthorProfileImageUrl()
            );
        }).toList();


        String nextCursor = null;
        if (rows.size() == size) {
            var last = rows.get(rows.size() - 1);
            nextCursor = FeedbackCursorCodec.encode(new FeedbackCursor(last.getCreatedAt(), last.getId()));
        }

        return new CursorPageResponse<>(items, nextCursor);
    }

    private String sanitize(String v) {
        if (v == null) throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Feedback comment is required");
        String t = v.trim().replaceAll("\\s{2,}", " ");
        if (t.isBlank()) throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Feedback comment is required");
        return t;
    }

    private FeedbackResponse map(SessionFeedback f) {
        boolean deleted = f.isDeleted();
        return new FeedbackResponse(
                f.getId(),
                f.getSessionId(),
                f.getRating(),
                deleted ? null : f.getComment(),
                f.getCreatedAt(),
                f.getEditedAt(),
                deleted,
                f.getAuthorUserId(),
                null, null, null, null, null
        );
    }
}

