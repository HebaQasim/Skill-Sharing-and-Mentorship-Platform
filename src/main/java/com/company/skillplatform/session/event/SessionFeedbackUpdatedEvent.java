package com.company.skillplatform.session.event;

import java.util.UUID;

public record SessionFeedbackUpdatedEvent(
        UUID feedbackId,
        UUID sessionId,
        UUID authorUserId
) {}
