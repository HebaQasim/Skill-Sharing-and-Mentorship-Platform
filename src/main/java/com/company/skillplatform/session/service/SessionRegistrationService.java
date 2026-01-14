package com.company.skillplatform.session.service;

import com.company.skillplatform.session.dto.SessionRegistrationResponse;

import java.util.UUID;

public interface SessionRegistrationService {

    SessionRegistrationResponse register(UUID userId, UUID sessionId);

    void cancel(UUID userId, UUID sessionId);
}
