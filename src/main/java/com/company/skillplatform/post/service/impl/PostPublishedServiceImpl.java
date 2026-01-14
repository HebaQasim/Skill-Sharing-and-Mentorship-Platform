package com.company.skillplatform.post.service.impl;

import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.exception.BusinessException;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ForbiddenException;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.post.dto.UpdatePublishedPostRequest;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.event.PostContentUpdatedEvent;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.post.service.PostPublishedService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostPublishedServiceImpl implements PostPublishedService {

    private final PostRepository postRepository;
    private final CacheStampService cacheStampService;
    private final ApplicationEventPublisher eventPublisher;

    public static final String FEED_STAMP_NAME = "postFeed";

    public void updateContent(UUID userId, UUID postId, UpdatePublishedPostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) throw new ForbiddenException("Not allowed");
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Post must be published");
        }

        String title = request.title();
        if (title != null) title = sanitize(title);

        String body = request.body();
        if (body != null) body = sanitize(body);


        if (body == null || body.trim().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Post body is required");
        }

        post.edit(title, body);

        cacheStampService.bump(FEED_STAMP_NAME);


        eventPublisher.publishEvent(new PostContentUpdatedEvent(postId, userId));
    }

    private String sanitize(String v) {
        String t = v.trim().replaceAll("\\s{2,}", " ");
        return t.isBlank() ? null : t;
    }
}

