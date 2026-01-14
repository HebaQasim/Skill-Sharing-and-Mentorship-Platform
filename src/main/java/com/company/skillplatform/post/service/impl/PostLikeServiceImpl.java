package com.company.skillplatform.post.service.impl;

import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.post.cursor.PostLikeCursor;
import com.company.skillplatform.post.cursor.PostLikeCursorCodec;
import com.company.skillplatform.post.dto.PostLikerResponse;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.entity.PostLike;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.repository.PostLikeRepository;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.post.service.PostLikeService;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeServiceImpl implements PostLikeService {

    public static final String POST_LIKERS_STAMP_NAME = "postLikers";
    public static final String POST_LIKERS_CACHE_NAME = "postLikersCache";

    private final PostRepository postRepository;
    private final PostLikeRepository likeRepository;
    private final NotificationRepository notificationRepository;
    private final CacheStampService cacheStampService;
    private final UserRepository userRepository;


    @Override
    public void like(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.POST_NOT_PUBLISHED, "You can like only published posts");
        }


        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return;
        }

        likeRepository.save(PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build());
        User liker = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND, "User not found"));

        cacheStampService.bump(POST_LIKERS_STAMP_NAME);
        if (!post.getAuthorUserId().equals(userId)) {
            notificationRepository.save(Notification.builder()
                    .recipientUserId(post.getAuthorUserId())
                    .type(NotificationType.POST_LIKED)
                    .title("New like")
                    .message(liker.getFullName() + " liked your post.")

                    .link("/posts/" + postId)
                    .build());
        }
    }

    @Override
    public void unlike(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.POST_NOT_PUBLISHED, "You can unlike only published posts");
        }


        likeRepository.deleteByPostIdAndUserId(postId, userId);
        cacheStampService.bump(POST_LIKERS_STAMP_NAME);

    }
    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<PostLikerResponse> likers(UUID postId, Integer limit, String cursor) {
        int size = normalizeLimit(limit);
        long stamp = cacheStampService.getStamp(POST_LIKERS_STAMP_NAME);
        return likersCached(stamp, postId, size, cursor);
    }

    @Cacheable(
            cacheNames = POST_LIKERS_CACHE_NAME,
            key = "T(com.company.skillplatform.common.cache.CacheKeys).postLikers(#stamp, #postId.toString(), #limit, #cursor)"
    )
    @Transactional(readOnly = true)
    public CursorPageResponse<PostLikerResponse> likersCached(long stamp, UUID postId, int limit, String cursor) {
        return likersInternal(postId, limit, cursor);
    }

    private CursorPageResponse<PostLikerResponse> likersInternal(UUID postId, int limit, String cursor) {

        // validate post exists + published
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found");
        }

        Pageable pageable = PageRequest.of(
                0,
                limit + 1,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        List<PostLikerResponse> rows;
        if (cursor == null || cursor.isBlank()) {
            rows = likeRepository.findLikersFirstPage(postId, pageable);
        } else {
            PostLikeCursor c = PostLikeCursorCodec.decode(cursor);
            rows = likeRepository.findLikersNextPage(postId, c.likedAt(), c.likeId(), pageable);
        }

        boolean hasNext = rows.size() > limit;
        if (hasNext) rows = rows.subList(0, limit);

        String nextCursor = null;
        if (hasNext && !rows.isEmpty()) {
            PostLikerResponse last = rows.get(rows.size() - 1);


            UUID likeId = likeRepository.findLikeId(postId, last.userId());
            if (likeId != null) {
                nextCursor = PostLikeCursorCodec.encode(new PostLikeCursor(last.likedAt(), likeId));
            }
        }

        return new CursorPageResponse<>(rows, nextCursor);
    }

    private int normalizeLimit(Integer limit) {
        int v = (limit == null) ? 20 : limit;
        v = Math.max(v, 1);
        v = Math.min(v, 50);
        return v;
    }
}
