package com.company.skillplatform.session.service;

import com.company.skillplatform.session.dto.AttachRecordingRequest;

import java.util.UUID;

public interface SessionRecordingService {
    void attachRecording(UUID hostUserId, UUID sessionId, AttachRecordingRequest request);
    void updateRecording(UUID hostUserId, UUID sessionId, AttachRecordingRequest request);
    void deleteRecording(UUID hostUserId, UUID sessionId);
}
