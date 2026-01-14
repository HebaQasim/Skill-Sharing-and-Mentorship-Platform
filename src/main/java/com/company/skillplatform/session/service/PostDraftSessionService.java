package com.company.skillplatform.session.service;

import com.company.skillplatform.session.dto.SessionResponse;
import com.company.skillplatform.session.dto.UpsertDraftSessionRequest;

import java.util.Optional;
import java.util.UUID;

public interface PostDraftSessionService {

    SessionResponse upsert(UUID userId, UUID postId, UpsertDraftSessionRequest request);

    void remove(UUID userId, UUID postId);

    Optional<SessionResponse> get(UUID userId, UUID postId);
}

