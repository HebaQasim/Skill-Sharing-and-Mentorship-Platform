package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.session.dto.AttachRecordingRequest;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.repository.*;

import com.company.skillplatform.session.service.SessionRecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionRecordingServiceImpl implements SessionRecordingService {

    private final SessionRepository sessionRepository;
    private final SessionAttendanceRepository attendanceRepository;
    private final SessionRegistrationRepository registrationRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public void attachRecording(UUID hostUserId, UUID sessionId, AttachRecordingRequest request) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SESSION_NOT_FOUND, "Session not found"));

        if (!session.getHostUserId().equals(hostUserId)) {
            throw new ForbiddenException("Not allowed");
        }
        String url = sanitizeUrl(request.recordingUrl());
        session.attachRecording(url);
        Set<UUID> recipients = new HashSet<>();

        recipients.addAll(attendanceRepository.findUserIdsBySessionId(sessionId));
        recipients.addAll(registrationRepository.findUserIdsBySessionId(sessionId));
        recipients.remove(hostUserId);
        if (!recipients.isEmpty()) {
            List<Notification> notifs = recipients.stream()
                    .map(uid -> Notification.builder()
                            .recipientUserId(uid)
                            .type(NotificationType.SESSION_RECORDING_ADDED)
                            .title("Session recording available")
                            .message("Recording for session '" + session.getTitle() + "' is now available.")
                            .link("/posts/" + session.getPostId()) // أو "/sessions/{id}"
                            .build())
                    .toList();

            notificationRepository.saveAll(notifs);
        }
    }

    @Transactional
    @Override
    public void updateRecording(UUID hostUserId, UUID sessionId, AttachRecordingRequest request) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SESSION_NOT_FOUND, "Session not found"));

        if (!session.getHostUserId().equals(hostUserId)) {
            throw new ForbiddenException("Not allowed");
        }
        if (session.getRecordingUrl() == null) {
            throw new BusinessException(
                    ErrorCode.CONFLICT, "No recording exists to update");
        }
        String url = sanitizeUrl(request.recordingUrl());
        session.attachRecording(url);

        notifyRecordingChanged(session, "Session recording updated");
    }

    @Transactional
    @Override
    public void deleteRecording(UUID hostUserId, UUID sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SESSION_NOT_FOUND, "Session not found"));

        if (!session.getHostUserId().equals(hostUserId)) {
            throw new ForbiddenException("Not allowed");
        }
        if (session.getRecordingUrl() == null) {
            throw new BusinessException(
                    ErrorCode.CONFLICT, "No recording exists to delete");
        }

        session.attachRecording(null);

    }
    private void notifyRecordingChanged(Session session, String title) {

        Set<UUID> recipients = new HashSet<>();

        recipients.addAll(attendanceRepository.findUserIdsBySessionId(session.getId()));
        recipients.addAll(registrationRepository.findUserIdsBySessionId(session.getId()));

        recipients.remove(session.getHostUserId());

        if (recipients.isEmpty()) return;

        List<Notification> notifications = recipients.stream()
                .map(uid -> Notification.builder()
                        .recipientUserId(uid)
                        .type(NotificationType.SESSION_RECORDING_ADDED)
                        .title(title)
                        .message("Recording for session '" + session.getTitle() + "' has been updated.")
                        .link("/posts/" + session.getPostId())
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
    }


    private String sanitizeUrl(String v) {
        String t = v == null ? "" : v.trim();
        if (t.isBlank()) throw new BusinessException(ErrorCode.VALIDATION_ERROR, "recordingUrl is required");
        return t;
    }
}

