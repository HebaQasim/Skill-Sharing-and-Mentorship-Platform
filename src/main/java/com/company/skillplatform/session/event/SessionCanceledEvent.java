package com.company.skillplatform.session.event;

import java.util.UUID;

public record SessionCanceledEvent(UUID sessionId, UUID postId) {}

