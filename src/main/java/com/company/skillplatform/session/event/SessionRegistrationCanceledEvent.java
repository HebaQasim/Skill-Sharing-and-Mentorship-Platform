package com.company.skillplatform.session.event;

import java.util.UUID;

public record SessionRegistrationCanceledEvent(
        UUID sessionId,
        UUID hostUserId,
        UUID attendeeUserId
) {}

