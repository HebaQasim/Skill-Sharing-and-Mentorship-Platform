package com.company.skillplatform.session.event;

import java.util.UUID;

public record SessionFeedbackAddedEvent(
        UUID feedbackId,
        UUID sessionId,
        UUID authorUserId
) {}