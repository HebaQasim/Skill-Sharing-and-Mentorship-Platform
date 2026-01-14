package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.session.dto.JoinSessionResponse;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.entity.SessionAttendance;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.repository.SessionAttendanceRepository;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.session.service.SessionJoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionJoinServiceImpl implements SessionJoinService {

    private final SessionRepository sessionRepository;
    private final SessionAttendanceRepository attendanceRepository;

    @Override
    public JoinSessionResponse join(UUID userId, UUID sessionId) {

        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SESSION_NOT_FOUND, "Session not found"));

        if (s.getStatus() != SessionStatus.LIVE) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Session is not live yet");
        }



        LocalDateTime now = LocalDateTime.now();
        if (s.getEndsAt() != null && now.isAfter(s.getEndsAt())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Session already ended");
        }


        attendanceRepository.findBySessionIdAndUserId(sessionId, userId)
                .ifPresentOrElse(existing -> {
                    existing.markJoinNow();
                }, () -> {
                    attendanceRepository.save(SessionAttendance.newFirstJoin(sessionId, userId));
                });


        String link = s.getMeetingLink();
        if (link == null || link.trim().isBlank()) {

            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Meeting link is not available");
        }
        return new JoinSessionResponse(link.trim());
    }
}

