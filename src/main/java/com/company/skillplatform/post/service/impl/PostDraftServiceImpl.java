package com.company.skillplatform.post.service.impl;

import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.post.cursor.DraftPostCursor;
import com.company.skillplatform.post.cursor.DraftPostCursorCodec;
import com.company.skillplatform.post.dto.DraftPostListItemResponse;
import com.company.skillplatform.post.dto.PostAttachmentResponse;
import com.company.skillplatform.post.dto.PostDraftResponse;
import com.company.skillplatform.post.dto.UpdatePostDraftRequest;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.mapper.PostAttachmentMapper;
import com.company.skillplatform.post.repository.PostAttachmentRepository;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.post.service.PostDraftService;
import com.company.skillplatform.session.dto.SessionMiniResponse;
import com.company.skillplatform.session.dto.SessionResponse;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.session.service.SessionConflictService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PostDraftServiceImpl implements PostDraftService {

    public static final String FEED_STAMP_NAME = "postFeed";

    private final PostRepository postRepository;
    private final CacheStampService cacheStampService;
    private final StorageService storageService;
    private final SessionRepository sessionRepository;
    private final PostAttachmentRepository attachmentRepository;
    private final PostAttachmentMapper attachmentMapper;
    private final SessionConflictService conflictService;

    @Override
    public PostDraftResponse createDraft(UUID userId) {
        Post draft = postRepository.save(
                Post.builder()
                        .authorUserId(userId)
                        .status(PostStatus.DRAFT)
                        .title(null)
                        .body(null)
                        .publishedAt(null)
                        .editedAt(null)
                        .build()
        );

        return toDraftResponse(draft);
    }

    @Override
    public PostDraftResponse updateDraft(UUID userId, UUID postId, UpdatePostDraftRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) {
            throw new ForbiddenException("Not allowed");
        }
        if (!post.isDraft()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Post is not a draft");
        }

        String title = request.title();
        if(title!=null){
            title=sanitizeTitle(title);
        }
        String body = request.body();
if(body!=null){
    body=sanitizeBody(body);
}
        post.edit(title, body);

        return toDraftResponse(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDraftResponse getDraft(UUID userId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) throw new ForbiddenException("Not allowed");
        if (!post.isDraft()) throw new BusinessException(ErrorCode.FORBIDDEN, "Post is not a draft");

        return toDraftResponse(post);
    }
    @Override
    public void publish(UUID userId, UUID postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.isOwner(userId)) throw new ForbiddenException("Not allowed");
        if (!post.isDraft()) throw new BusinessException(ErrorCode.FORBIDDEN, "Post is not a draft");

        if (post.getBody() == null || post.getBody().trim().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Post body is required to publish");
        }

        //  If session exists, validate conflicts BEFORE publish
        sessionRepository.findByPostId(post.getId()).ifPresent(session -> {

            LocalDateTime startsAt = session.getStartsAt();
            LocalDateTime endAt = session.getEndsAt();
            UUID excludeSessionId = session.getId();

            conflictService.assertHostNoBlockingConflicts(userId, startsAt, endAt, excludeSessionId);
        });

        // publish post
        post.publish();

        // schedule session if exists
        sessionRepository.findByPostId(post.getId())
                .ifPresent(Session::scheduleOnPublish);

        cacheStampService.bump(FEED_STAMP_NAME);
    }

    @Override
    public CursorPageResponse<DraftPostListItemResponse> myDrafts(UUID userId, Integer limit, String cursor) {
        int size = normalizeLimit(limit);
        Pageable pageable = PageRequest.of(
                0, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))
        );

        List<Post> posts;

        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.findMyDraftsFirstPage(userId, PostStatus.DRAFT, pageable);
        } else {
            DraftPostCursor c = DraftPostCursorCodec.decode(cursor);
            posts = postRepository.findMyDraftsAfter(
                    userId,
                    PostStatus.DRAFT,
                    c.createdAt(),
                    c.id(),
                    pageable
            );
        }

        var items = posts.stream().map(this::toItem).toList();

        String nextCursor = null;
        if (posts.size() == size) {
            Post last = posts.get(posts.size() - 1);
            nextCursor = DraftPostCursorCodec.encode(new DraftPostCursor(last.getCreatedAt(), last.getId()));
        }

        return new CursorPageResponse<>(items, nextCursor);
    }

    private DraftPostListItemResponse toItem(Post p) {
        return new DraftPostListItemResponse(
                p.getId(),
                p.getTitle(),
                preview(p.getBody(), 200),
                p.getCreatedAt(),
                p.getEditedAt()
        );
    }



    private PostDraftResponse toDraftResponse(Post p) {
        List<PostAttachmentResponse> attachments =
                attachmentRepository.findByPostIdOrderByCreatedAtAsc(p.getId())
                        .stream()
                        .map(attachmentMapper::toResponse)
                        .toList();
        var session = sessionRepository.findByPostId(p.getId())
                .map(s -> new SessionMiniResponse(
                        s.getId(),
                        s.getTitle(),
                        s.getStartsAt(),
                        s.getDurationMinutes(),
                        s.getMeetingLink(),
                        s.getStatus().name()
                ))
                .orElse(null);


        return new PostDraftResponse(
                p.getId(),
                p.getStatus().name(),
                p.getTitle(),
                p.getBody(),
                p.getCreatedAt(),
                p.getEditedAt(),
                attachments,
                session
        );
    }

    private String sanitizeTitle(String v) {
        String t = v.trim().replaceAll("\\s{2,}", " ");
        return  t;
    }

    private String sanitizeBody(String v) {

        String t = v.trim();
        return t.replaceAll("\\s{2,}", " ");
    }
    private String preview(String body, int max) {
        if (body == null) return null;
        String t = body.trim();
        if (t.isEmpty()) return null;
        return (t.length() <= max) ? t : (t.substring(0, max) + "...");
    }

    private int normalizeLimit(Integer limit) {
        int v = (limit == null) ? 20 : limit;
        if (v < 1) v = 1;
        if (v > 50) v = 50;
        return v;
    }

}

