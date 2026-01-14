package com.company.skillplatform.session.cursor;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionCursor(LocalDateTime startsAt, UUID id) {}
