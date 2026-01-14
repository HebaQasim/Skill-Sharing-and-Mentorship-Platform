package com.company.skillplatform.post.cursor;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentCursor(LocalDateTime createdAt, UUID id) {}
