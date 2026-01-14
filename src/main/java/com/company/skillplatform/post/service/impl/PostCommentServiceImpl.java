package com.company.skillplatform.post.service.impl;

import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.cache.CacheKeys;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.post.cursor.*;
import com.company.skillplatform.post.dto.*;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.entity.PostComment;
import com.company.skillplatform.post.repository.*;
import com.company.skillplatform.post.service.PostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommentServiceImpl implements PostCommentService {

    private static final String COMMENTS_CACHE = "comments_cache";

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final NotificationRepository notificationRepository;
    private final CacheStampService cacheStampService;
    private final StorageService storageService;

    @Override
    public CommentResponse add(UUID userId, UUID postId, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        String body = sanitizeBody(request.body());

        PostComment saved = commentRepository.save(PostComment.builder()
                .postId(postId)
                .authorUserId(userId)
                .body(body)
                .build());

        // bump comments stamp for this post (invalidate cached first pages)
        bumpCommentsStamp(postId);
        cacheStampService.bump(PostFeedServiceImpl.FEED_STAMP_NAME);
        // notify post owner + previous commenters (excluding actor)
        notifyNewComment(post, userId, saved.getId(), body);

        return map(saved); // lightweight map (no author card here)
    }

    @Override
    public CommentResponse edit(UUID userId, UUID postId, UUID commentId, EditCommentRequest request) {
        PostComment c = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMMENT_NOT_FOUND, "Comment not found"));

        if (!c.isOwner(userId)) throw new ForbiddenException("Not allowed");
        if (c.isDeleted()) throw new BusinessException(ErrorCode.COMMENT_NOT_ALLOWED, "Comment is deleted");

        c.edit(sanitizeBody(request.body()));

        bumpCommentsStamp(postId);
        cacheStampService.bump(PostFeedServiceImpl.FEED_STAMP_NAME);
        return map(c);
    }

    @Override
    public void delete(UUID userId, UUID postId, UUID commentId) {
        PostComment c = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMMENT_NOT_FOUND, "Comment not found"));

        if (!c.isOwner(userId)) throw new ForbiddenException("Not allowed");

        c.softDelete();

        bumpCommentsStamp(postId);
        cacheStampService.bump(PostFeedServiceImpl.FEED_STAMP_NAME);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> list(UUID userId, UUID postId, Integer limit, String cursor) {

        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        int size = (limit == null) ? 20 : Math.min(Math.max(limit, 1), 50);
        boolean firstPage = (cursor == null || cursor.isBlank());

        long stamp = cacheStampService.getStamp(commentsStampName(postId));

        if (firstPage && size == 20) {
            return listTop20Cached(postId, stamp);
        }

        return fetchUncached(postId, size, cursor);
    }

    @Cacheable(cacheNames = COMMENTS_CACHE,
            key = "T(CacheKeys).postCommentsTop20(#postId, #stamp)")
    @Transactional(readOnly = true)
    public CursorPageResponse<CommentResponse> listTop20Cached(UUID postId, long stamp) {
        return fetchUncached(postId, 20, null);
    }

    private CursorPageResponse<CommentResponse> fetchUncached(UUID postId, int size, String cursor) {
        var pageable = PageRequest.of(0, size);
        List<CommentRow> rows;

        if (cursor == null || cursor.isBlank()) {
            rows = commentRepository.firstPage(postId, pageable);
        } else {
            CommentCursor c = CommentCursorCodec.decode(cursor);
            rows = commentRepository.nextPage(postId, c.createdAt(), c.id(), pageable);
        }

        var items = rows.stream().map(this::mapRow).toList();

        String nextCursor = null;
        if (rows.size() == size) {
            CommentRow last = rows.get(rows.size() - 1);
            nextCursor = CommentCursorCodec.encode(new CommentCursor(last.getCreatedAt(), last.getId()));
        }

        return new CursorPageResponse<>(items, nextCursor);
    }

    // ---------- Notifications ----------
    private void notifyNewComment(Post post, UUID actorUserId, UUID commentId, String body) {

        Set<UUID> recipients = new HashSet<>();

        // 1) post owner
        recipients.add(post.getAuthorUserId());

        // 2) all previous commenters
        recipients.addAll(commentRepository.distinctActiveCommenterIds(post.getId()));

        // remove actor
        recipients.remove(actorUserId);

        if (recipients.isEmpty()) return;

        String title = "New comment";
        String message = "A new comment was added.";
        String link = "/posts/" + post.getId();

        for (UUID recipientId : recipients) {
            notificationRepository.save(Notification.builder()
                    .recipientUserId(recipientId)
                    .type(NotificationType.POST_COMMENTED)
                    .title(title)
                    .message(message)
                    .link(link)
                    .build());
        }
    }

    // ---------- helpers ----------
    private void bumpCommentsStamp(UUID postId) {
        cacheStampService.bump(commentsStampName(postId));
    }

    private String commentsStampName(UUID postId) {
        return "post_comments:" + postId;
    }

    private String sanitizeBody(String v) {
        if (v == null) throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Comment body is required");
        String t = v.trim().replaceAll("\\s{2,}", " ");
        if (t.isBlank()) throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Comment body is required");
        return t;
    }

    private CommentResponse map(PostComment c) {

        return new CommentResponse(
                c.getId(),
                c.getPostId(),
                c.isDeleted() ? null : c.getBody(),
                c.getCreatedAt(),
                c.getEditedAt(),
                c.isDeleted(),
                c.getAuthorUserId(),
                null, null, null, null, null
        );
    }

    private CommentResponse mapRow(CommentRow r) {
        String url = (r.getAuthorProfileImageUrl() == null) ? null : storageService.signedUrl(r.getAuthorProfileImageUrl());
        boolean deleted = r.getDeletedAt() != null;

        return new CommentResponse(
                r.getId(),
                r.getPostId(),
                deleted ? null : r.getBody(),
                r.getCreatedAt(),
                r.getEditedAt(),
                deleted,
                r.getAuthorId(),
                r.getAuthorFullName(),
                r.getAuthorDepartment(),
                r.getAuthorJobTitle(),
                r.getAuthorHeadline(),
               url
        );
    }
}
