package com.company.skillplatform.session.service;

import com.company.skillplatform.session.dto.SessionPersonRow;

import java.util.List;
import java.util.UUID;

public interface SessionPeopleService {
    List<SessionPersonRow> registered(UUID hostUserId, UUID sessionId);
    List<SessionPersonRow> attended(UUID hostUserId, UUID sessionId);
}

