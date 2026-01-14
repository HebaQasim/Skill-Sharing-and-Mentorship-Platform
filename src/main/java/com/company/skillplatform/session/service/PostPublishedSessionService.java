package com.company.skillplatform.session.service;

import com.company.skillplatform.session.dto.SessionResponse;
import com.company.skillplatform.session.dto.UpdatePublishedSessionRequest;

import java.util.UUID;

public interface PostPublishedSessionService {
    SessionResponse upsertOrUpdate(UUID userId, UUID postId, UpdatePublishedSessionRequest request);

    void cancel(UUID userId, UUID postId);

}
