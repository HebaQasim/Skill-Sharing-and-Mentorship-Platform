package com.company.skillplatform.post.service.impl;

import com.company.skillplatform.common.cache.CacheKeys;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.post.cursor.PostCursor;
import com.company.skillplatform.post.cursor.PostCursorCodec;
import com.company.skillplatform.post.dto.PostCardCached;
import com.company.skillplatform.post.dto.PostCardResponse;
import com.company.skillplatform.post.dto.PostDetailsResponse;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.entity.PostAttachment;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.repository.PostAttachmentRepository;
import com.company.skillplatform.post.repository.PostCommentRepository;
import com.company.skillplatform.post.repository.PostLikeRepository;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.post.service.PostFeedService;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.repository.SessionFeedbackRepository;
import com.company.skillplatform.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostFeedServiceImpl implements PostFeedService {

    public static final String FEED_STAMP_NAME = "postFeed";
    public static final String FEED_CACHE_NAME = "postFeedCache";

    private final PostRepository postRepository;
    private final PostAttachmentRepository attachmentRepository;
    private final SessionRepository sessionRepository;
    private final StorageService storageService;
    private final CacheStampService cacheStampService;

    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final SessionFeedbackRepository sessionFeedbackRepository;


    @Override
    public CursorPageResponse<PostCardResponse> feed(UUID userId, Integer limit, String cursor) {
        int size = normalize(limit);


        long stamp = cacheStampService.getStamp(FEED_STAMP_NAME);


        CursorPageResponse<PostCardCached> shared = feedShared(stamp, size, cursor);


        List<PostCardCached> baseItems = shared.items();
        if (baseItems.isEmpty()) {
            return new CursorPageResponse<>(List.of(), shared.nextCursor());
        }

        List<UUID> postIds = baseItems.stream().map(PostCardCached::id).toList();


        var likedRows = postLikeRepository.findLikedPostIds(userId, postIds);
        Set<UUID> likedSet = new HashSet<>();
        for (var r : likedRows) {
            likedSet.add(r.getPostId());
        }


        List<PostCardResponse> finalItems = baseItems.stream()
                .map(cached -> new PostCardResponse(
                        cached.id(),
                        cached.title(),
                        cached.bodyPreview(),
                        cached.publishedAt(),
                        cached.author(),
                        cached.likesCount(),
                        likedSet.contains(cached.id()),
                        cached.commentsCount(),
                        cached.attachments(),
                        cached.session()
                ))
                .toList();

        return new CursorPageResponse<>(finalItems, shared.nextCursor());
    }


    @Cacheable(
            cacheNames = FEED_CACHE_NAME,
            key = "T(CacheKeys).postFeedShared(#stamp, #limit, #cursor)"
    )
    public CursorPageResponse<PostCardCached> feedShared(long stamp, int limit, String cursor) {
        return feedSharedInternal(limit, cursor);
    }


    private CursorPageResponse<PostCardCached> feedSharedInternal(int limit, String cursor) {

        var pageable = PageRequest.of(
                0,
                limit + 1,
                Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id"))
        );

        List<Post> posts;
        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.feedFirstPage(PostStatus.PUBLISHED, pageable);
        } else {
            PostCursor c = PostCursorCodec.decode(cursor);
            posts = postRepository.feedNextPage(PostStatus.PUBLISHED, c.publishedAt(), c.id(), pageable);
        }

        boolean hasNext = posts.size() > limit;
        if (hasNext) posts = posts.subList(0, limit);

        if (posts.isEmpty()) {
            return new CursorPageResponse<>(List.of(), null);
        }

        List<UUID> postIds = posts.stream().map(Post::getId).toList();

        // 1) likesCount batch
        var likeCounts = postLikeRepository.countByPostIds(postIds);
        Map<UUID, Long> likeCountMap = new HashMap<>();
        for (var row : likeCounts) {
            likeCountMap.put(row.getPostId(), row.getCnt());
        }

        // 2) commentsCount batch
        var commentCounts = postCommentRepository.countActiveByPostIds(postIds);
        Map<UUID, Long> commentCountMap = new HashMap<>();
        for (var row : commentCounts) {
            commentCountMap.put(row.getPostId(), row.getCnt());
        }

        // 3) attachments batch
        Map<UUID, List<PostAttachment>> attMap = groupAttachments(postIds);

        // 4) sessions batch
        Map<UUID, Session> sessionMap = groupSessions(postIds);


        List<PostCardCached> items = posts.stream()
                .map(p -> toCachedCard(
                        p,
                        attMap.getOrDefault(p.getId(), List.of()),
                        sessionMap.get(p.getId()),
                        likeCountMap,
                        commentCountMap
                ))
                .toList();

        String nextCursor = null;
        if (hasNext) {
            Post last = posts.get(posts.size() - 1);
            nextCursor = PostCursorCodec.encode(new PostCursor(last.getPublishedAt(), last.getId()));
        }

        return new CursorPageResponse<>(items, nextCursor);
    }

    private PostCardCached toCachedCard(
            Post p,
            List<PostAttachment> atts,
            Session session,
            Map<UUID, Long> likeCountMap,
            Map<UUID, Long> commentCountMap
    ) {
        String bodyPreview = preview(p.getBody(), 220);

        var u = p.getAuthor();
        String url = (u.getProfileImageUrl() == null) ? null : storageService.signedUrl(u.getProfileImageUrl());
        PostCardResponse.AuthorCard authorCard = new PostCardResponse.AuthorCard(
                u.getId(),
                u.getFullName(),
                u.getDepartment(),
                u.getJobTitle(),
                u.getHeadline(),
                url
        );

        List<PostCardResponse.AttachmentMini> minis = atts.stream()
                .limit(3)
                .map(a -> new PostCardResponse.AttachmentMini(
                        a.getId(),
                        a.getType().name(),
                        storageService.signedUrl(a.getStorageKey())
                ))
                .toList();

        PostCardResponse.SessionMini sessionMini = null;
        if (session != null) {
            sessionMini = new PostCardResponse.SessionMini(
                    session.getId(),
                    session.getTitle(),
                    session.getStartsAt(),
                    session.getDurationMinutes(),
                    session.getStatus().name()
            );
        }

        long likesCount = likeCountMap.getOrDefault(p.getId(), 0L);
        long commentsCount = commentCountMap.getOrDefault(p.getId(), 0L);

        return new PostCardCached(
                p.getId(),
                p.getTitle(),
                bodyPreview,
                p.getPublishedAt(),
                authorCard,
                likesCount,
                commentsCount,
                minis,
                sessionMini
        );
    }



    @Override
    public PostDetailsResponse getPost(UUID viewerUserId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));

        if (post.getStatus() == PostStatus.DELETED || post.getStatus() != PostStatus.PUBLISHED) {
            throw new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found");
        }

        var attachments = attachmentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        var sessionOpt = sessionRepository.findByPostId(postId);

        var author = post.getAuthor();
        String url = (author.getProfileImageUrl() == null) ? null : storageService.signedUrl(author.getProfileImageUrl());
        PostCardResponse.AuthorCard authorCard = new PostCardResponse.AuthorCard(
                author.getId(),
                author.getFullName(),
                author.getDepartment(),
                author.getJobTitle(),
                author.getHeadline(),
                url
        );

        List<PostDetailsResponse.AttachmentFull> attFull = attachments.stream()
                .map(a -> new PostDetailsResponse.AttachmentFull(
                        a.getId(),
                        a.getType().name(),
                        a.getOriginalFilename(),
                        a.getContentType(),
                        a.getSizeBytes(),
                        storageService.signedUrl(a.getStorageKey()),
                        a.getCreatedAt()
                ))
                .toList();

        PostDetailsResponse.SessionFull session = sessionOpt
                .map(s -> new PostDetailsResponse.SessionFull(
                        s.getId(),
                        s.getTitle(),
                        s.getStartsAt(),
                        s.getDurationMinutes(),
                        s.getMeetingLink(),
                        s.getStatus().name()
                ))
                .orElse(null);

        long likesCount = postLikeRepository.countByPostId(postId);
        long commentsCount = postCommentRepository.countActiveByPostId(postId);
        boolean likedByMe = postLikeRepository.existsByPostIdAndUserId(postId, viewerUserId);
        long feedbacksCount = sessionFeedbackRepository.countByPostId(postId);

        return new PostDetailsResponse(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getPublishedAt(),
                post.getEditedAt(),
                likesCount,
                likedByMe,
                commentsCount,
                authorCard,
                attFull,
                session,
                feedbacksCount
        );
    }

    // ====== helpers ======

    private Map<UUID, List<PostAttachment>> groupAttachments(List<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Map.of();

        List<PostAttachment> all = attachmentRepository.findByPostIdInOrderByPostIdAscCreatedAtAsc(postIds);

        Map<UUID, List<PostAttachment>> map = new HashMap<>();
        for (PostAttachment a : all) {
            map.computeIfAbsent(a.getPostId(), k -> new ArrayList<>()).add(a);
        }
        return map;
    }

    private Map<UUID, Session> groupSessions(List<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) return Map.of();

        List<Session> all = sessionRepository.findByPostIdIn(postIds);

        Map<UUID, Session> map = new HashMap<>();
        for (Session s : all) {
            map.put(s.getPostId(), s);
        }
        return map;
    }

    private String preview(String body, int max) {
        if (body == null) return null;
        String t = body.trim();
        if (t.length() <= max) return t;
        return t.substring(0, max) + "...";
    }

    private int normalize(Integer limit) {
        int v = (limit == null) ? 20 : limit;
        v = Math.max(v, 1);
        v = Math.min(v, 50);
        return v;
    }
}
