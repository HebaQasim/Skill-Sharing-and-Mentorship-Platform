package com.company.skillplatform.session.event;

import java.util.UUID;

public record SessionCompletedEvent(UUID sessionId, UUID hostUserId) {}

