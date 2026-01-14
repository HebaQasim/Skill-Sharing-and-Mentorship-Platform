package com.company.skillplatform.post.service;

import java.util.UUID;

public interface PostSoftDeleteService {
    void softDelete(UUID userId, UUID postId);
    void restore(UUID userId, UUID postId);
}
