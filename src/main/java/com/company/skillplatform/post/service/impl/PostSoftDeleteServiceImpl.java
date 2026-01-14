package com.company.skillplatform.post.service.impl;

import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.exception.BusinessException;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ForbiddenException;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.post.service.PostSoftDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.company.skillplatform.post.service.impl.PostPublishedServiceImpl.FEED_STAMP_NAME;

@Service
@RequiredArgsConstructor
@Transactional
public class PostSoftDeleteServiceImpl implements PostSoftDeleteService {
    private final PostRepository postRepository;
    private final CacheStampService cacheStampService;
  @Override
    public void softDelete(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) throw new ForbiddenException("Not allowed");

        if (post.getStatus() == PostStatus.DELETED) return;

        post.markDeleted();
        cacheStampService.bump(PostFeedServiceImpl.FEED_STAMP_NAME);
      cacheStampService.bump(FEED_STAMP_NAME);
    }

    @Transactional
    public void restore(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) throw new ForbiddenException("Not allowed");

        if (post.getStatus() != PostStatus.DELETED) {
            throw new BusinessException(ErrorCode.CONFLICT, "Post is not deleted");
        }

        post.restorePublished();
        cacheStampService.bump(PostFeedServiceImpl.FEED_STAMP_NAME);
        cacheStampService.bump(FEED_STAMP_NAME);
    }

}
