package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ForbiddenException;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.session.dto.SessionPersonRow;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.repository.SessionAttendanceRepository;
import com.company.skillplatform.session.repository.SessionRegistrationRepository;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.session.service.SessionPeopleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionPeopleServiceImpl implements SessionPeopleService {

    private final SessionRepository sessionRepository;
    private final SessionRegistrationRepository registrationRepository;
    private final SessionAttendanceRepository attendanceRepository;

    @Override
    public List<SessionPersonRow> registered(UUID hostUserId, UUID sessionId) {
        mustBeHost(hostUserId, sessionId);
        return registrationRepository.listRegistered(sessionId).stream()
                .map(r -> new SessionPersonRow(r.getUserId(), r.getFullName(), r.getDepartment(), r.getJobTitle(), r.getJoinedAt()))
                .toList();
    }

    @Override
    public List<SessionPersonRow> attended(UUID hostUserId, UUID sessionId) {
        mustBeHost(hostUserId, sessionId);
        return attendanceRepository.listAttended(sessionId).stream()
                .map(a -> new SessionPersonRow(a.getUserId(), a.getFullName(), a.getDepartment(), a.getJobTitle(), a.getJoinedAt()))
                .toList();
    }

    private void mustBeHost(UUID hostUserId, UUID sessionId) {
        Session s = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SESSION_NOT_FOUND, "Session not found"));
        if (!s.getHostUserId().equals(hostUserId)) {
            throw new ForbiddenException("Not allowed");
        }
    }
}

