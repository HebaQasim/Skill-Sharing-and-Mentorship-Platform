package com.company.skillplatform.session.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.session.dto.HostedSessionCardResponse;
import com.company.skillplatform.session.enums.SessionStatus;

import java.util.Set;
import java.util.UUID;

public interface UserHostedSessionsService {
    CursorPageResponse<HostedSessionCardResponse> getHostedSessions(
            UUID hostUserId,
            String cursor,
            Integer size,
            Set<SessionStatus> statuses
    );
}
