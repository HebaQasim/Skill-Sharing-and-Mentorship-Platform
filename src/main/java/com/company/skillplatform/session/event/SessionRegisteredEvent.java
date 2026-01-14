package com.company.skillplatform.session.event;

import java.util.UUID;

public record SessionRegisteredEvent(
        UUID sessionId,
        UUID hostUserId,
        UUID attendeeUserId
) {}

