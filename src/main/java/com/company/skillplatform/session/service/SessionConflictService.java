package com.company.skillplatform.session.service;



import com.company.skillplatform.session.repository.SessionConflictRow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SessionConflictService {

    void assertHostNoBlockingConflicts(UUID hostUserId, LocalDateTime startsAt, LocalDateTime endAt, UUID excludeSessionId);

    List<SessionConflictRow> findDepartmentConflictsForWarning(String department, LocalDateTime startsAt, LocalDateTime endAt, UUID excludeSessionId);
}
