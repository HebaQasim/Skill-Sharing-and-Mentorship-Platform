package com.company.skillplatform.post.cursor;

import java.time.LocalDateTime;
import java.util.UUID;

public record DraftPostCursor(LocalDateTime createdAt, UUID id) {}
