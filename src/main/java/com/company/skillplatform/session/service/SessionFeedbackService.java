package com.company.skillplatform.session.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.session.dto.*;

import java.util.UUID;

public interface SessionFeedbackService {
    FeedbackResponse add(UUID userId, UUID sessionId, CreateFeedbackRequest request);
    FeedbackResponse edit(UUID userId, UUID sessionId, UUID feedbackId, EditFeedbackRequest request);
    void delete(UUID userId, UUID sessionId, UUID feedbackId);

    CursorPageResponse<FeedbackResponse> list(UUID sessionId, Integer limit, String cursor);
}
