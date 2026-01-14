package com.company.skillplatform.session.service.impl;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.session.cursor.*;
import com.company.skillplatform.session.dto.HostedSessionCardResponse;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.session.service.UserHostedSessionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserHostedSessionsServiceImpl implements UserHostedSessionsService {

    private final SessionRepository sessionRepository;
@Override
@Transactional(readOnly = true)
public CursorPageResponse<HostedSessionCardResponse> getHostedSessions(
        UUID hostUserId,
        String cursor,
        Integer size,
        Set<SessionStatus> statuses
) {
    int pageSize = (size == null) ? 20 : Math.min(Math.max(size, 1), 50);
    var pageable = PageRequest.of(0, pageSize);

    SessionCursor c = (cursor == null || cursor.isBlank()) ? null : SessionCursorCodec.decode(cursor);

    // default: include all statuses
    Set<SessionStatus> active = (statuses == null || statuses.isEmpty()) ? EnumSet.allOf(SessionStatus.class) : statuses;

    var items = sessionRepository.findHostedSessionsPage(
            hostUserId,
            active,
            c == null ? null : c.startsAt(),
            c == null ? null : c.id(),
            pageable
    );

    String nextCursor = null;
    if (items.size() == pageSize) {
        HostedSessionCardResponse last = items.get(items.size() - 1);
        nextCursor = SessionCursorCodec.encode(new SessionCursor(last.startsAt(), last.sessionId()));
    }

    return new CursorPageResponse<>(items, nextCursor);
}

}

