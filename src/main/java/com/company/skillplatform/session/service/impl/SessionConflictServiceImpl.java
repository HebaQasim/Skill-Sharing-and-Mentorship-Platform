package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.exception.BusinessException;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.repository.SessionConflictRow;
import com.company.skillplatform.session.repository.SessionRegistrationRepository;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.session.service.SessionConflictService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionConflictServiceImpl implements SessionConflictService {

    private static final EnumSet<SessionStatus> ACTIVE = EnumSet.of(SessionStatus.SCHEDULED,SessionStatus.LIVE);

    private final SessionRepository sessionRepository;
    private final SessionRegistrationRepository registrationRepository;

    @Override
    public void assertHostNoBlockingConflicts(UUID hostUserId, LocalDateTime startsAt, LocalDateTime endAt, UUID excludeSessionId) {

        long hostOverlaps = sessionRepository.countHostOverlaps(hostUserId, startsAt, endAt, excludeSessionId, ACTIVE);
        if (hostOverlaps > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "You already host another session at the same time");
        }

        long registeredOverlaps = registrationRepository.countUserRegisteredOverlaps(hostUserId, startsAt, endAt, ACTIVE);
        if (registeredOverlaps > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "You are registered for another session at the same time");
        }
    }

    @Override
    public List<SessionConflictRow> findDepartmentConflictsForWarning(String department, LocalDateTime startsAt, LocalDateTime endAt, UUID excludeSessionId) {
        return sessionRepository.findDepartmentOverlaps(
                department,
                startsAt,
                endAt,
                excludeSessionId,
                ACTIVE,
                PageRequest.of(0, 5)
        );
    }
}
