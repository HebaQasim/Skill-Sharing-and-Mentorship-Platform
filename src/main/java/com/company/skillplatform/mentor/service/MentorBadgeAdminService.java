package com.company.skillplatform.mentor.service;

import com.company.skillplatform.mentor.dto.DecideMentorRequest;

import java.util.UUID;

public interface MentorBadgeAdminService {
    void approve(UUID adminId, UUID requestId, DecideMentorRequest req);
    void reject(UUID adminId, UUID requestId, DecideMentorRequest req);
}

