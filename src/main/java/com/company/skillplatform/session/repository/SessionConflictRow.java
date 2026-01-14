package com.company.skillplatform.session.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SessionConflictRow {
    UUID getId();
    String getTitle();
    LocalDateTime getStartsAt();
    LocalDateTime getEndAt();
}
