package com.company.skillplatform.session.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface FeedbackRow {
    UUID getId();
    UUID getSessionId();
    Integer getRating();
    String getComment();
    LocalDateTime getCreatedAt();
    LocalDateTime getEditedAt();
    LocalDateTime getDeletedAt();

    UUID getAuthorId();
    String getAuthorFullName();
    String getAuthorDepartment();
    String getAuthorJobTitle();
    String getAuthorHeadline();
    String getAuthorProfileImageUrl();
}
