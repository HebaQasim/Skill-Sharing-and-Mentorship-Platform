package com.company.skillplatform.post.service;

import com.company.skillplatform.post.dto.UpdatePublishedPostRequest;

import java.util.UUID;

public interface PostPublishedService {
    void updateContent(UUID userId, UUID postId, UpdatePublishedPostRequest request);
}
