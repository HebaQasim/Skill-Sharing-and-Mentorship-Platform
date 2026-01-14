package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.session.dto.SessionRegistrationResponse;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.entity.SessionRegistration;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.event.SessionRegisteredEvent;
import com.company.skillplatform.session.event.SessionRegistrationCanceledEvent;
import com.company.skillplatform.session.repository.SessionRegistrationRepository;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.session.service.SessionRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionRegistrationServiceImpl implements SessionRegistrationService {

    private final SessionRepository sessionRepository;
    private final SessionRegistrationRepository registrationRepository;
    private final ApplicationEventPublisher publisher;


    @Override
    public SessionRegistrationResponse register(UUID userId, UUID sessionId) {

        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SESSION_NOT_FOUND, "Session not found"));

        // must be scheduled
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Session is not open for registration");
        }

        // must be in the future
        if (session.getStartsAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Session already started");
        }

       // host cannot register
        if (session.getHostUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Host cannot register to own session");
        }

        // already registered?
        if (registrationRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Already registered");
        }

        LocalDateTime startsAt = session.getStartsAt();
        LocalDateTime endAt = session.getEndsAt();

        // conflict #1: registered overlap
        if (registrationRepository.existsRegisteredOverlap(userId, startsAt, endAt, sessionId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "You are registered in another session at this time");
        }

        // conflict #2: hosted overlap
        if (sessionRepository.existsHostedOverlap(userId, startsAt, endAt, sessionId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "You have another scheduled session at this time");
        }

        SessionRegistration saved = registrationRepository.save(SessionRegistration.builder()
                .sessionId(sessionId)
                .userId(userId)
                .build());

        publisher.publishEvent(new SessionRegisteredEvent(
                session.getId(),
                session.getHostUserId(),
                userId
        ));


        return new SessionRegistrationResponse(
                sessionId,
                userId,
                saved.getCreatedAt()
        );
    }

    @Override
    public void cancel(UUID userId, UUID sessionId) {

        if (!registrationRepository.existsBySessionIdAndUserId(sessionId, userId)) {
            return;
        }
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SESSION_NOT_FOUND, "Session not found"));

        registrationRepository.deleteBySessionIdAndUserId(sessionId, userId);
        publisher.publishEvent(new SessionRegistrationCanceledEvent(
                session.getId(),
                session.getHostUserId(),
                userId
        ));
    }
}
