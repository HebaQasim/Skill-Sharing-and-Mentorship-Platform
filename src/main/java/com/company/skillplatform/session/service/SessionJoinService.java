package com.company.skillplatform.session.service;

import com.company.skillplatform.session.dto.JoinSessionResponse;

import java.util.UUID;

public interface SessionJoinService {
    JoinSessionResponse join(UUID userId, UUID sessionId);
}
