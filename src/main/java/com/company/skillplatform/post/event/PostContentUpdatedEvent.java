package com.company.skillplatform.post.event;

import java.util.UUID;

public record PostContentUpdatedEvent(
        UUID postId,
        UUID editorUserId
) {}
