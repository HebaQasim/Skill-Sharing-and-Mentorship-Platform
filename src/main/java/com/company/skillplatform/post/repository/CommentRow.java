package com.company.skillplatform.post.repository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CommentRow {
    UUID getId();
    UUID getPostId();
    String getBody();
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
