package com.company.skillplatform.session.event;

import java.util.UUID;

public record SessionUpdatedEvent(UUID sessionId, UUID postId) {}

