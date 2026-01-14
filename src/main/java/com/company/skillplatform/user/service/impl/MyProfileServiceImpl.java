package com.company.skillplatform.user.service.impl;

import com.company.skillplatform.approval.payload.ProfileChangePayload;
import com.company.skillplatform.approval.service.ApprovalRequestService;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.common.storage.StorageService;
import com.company.skillplatform.common.util.Texts;
import com.company.skillplatform.common.web.PageRequests;
import com.company.skillplatform.post.cursor.PostCursor;
import com.company.skillplatform.post.cursor.PostCursorCodec;
import com.company.skillplatform.post.dto.PostCardCached;
import com.company.skillplatform.post.dto.PostCardResponse;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.entity.PostAttachment;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.repository.PostAttachmentRepository;
import com.company.skillplatform.post.repository.PostCommentRepository;
import com.company.skillplatform.post.repository.PostLikeRepository;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.post.service.impl.PostFeedServiceImpl;
import com.company.skillplatform.session.entity.Session;
import com.company.skillplatform.session.repository.SessionRepository;
import com.company.skillplatform.skill.dto.SkillResponse;
import com.company.skillplatform.skill.enums.SkillSort;
import com.company.skillplatform.skill.repository.SkillRepository;
import com.company.skillplatform.user.dto.MyProfileResponse;
import com.company.skillplatform.user.dto.MyProfileWithPostsResponse;
import com.company.skillplatform.user.dto.UpdateMyProfileRequest;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.mapper.UserMapper;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.user.service.MyProfileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MyProfileServiceImpl implements MyProfileService {
    public static final String PROFILE_POSTS_CACHE = "profilePostsCache";
    public static final String PROFILE_POSTS_STAMP = PostFeedServiceImpl.FEED_STAMP_NAME;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final ApprovalRequestService approvalRequestService;
    private final ObjectMapper objectMapper;
    private final SkillRepository skillRepository;
    private final CacheStampService cacheStampService;
    private final PostRepository postRepository;
    private final PostAttachmentRepository attachmentRepository;
    private final SessionRepository sessionRepository;
    private final StorageService storageService;

    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;

@Override
    @Transactional(readOnly = true)
    public MyProfileWithPostsResponse meWithPosts(UUID userId, Integer limit, String cursor) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        // ----- profile base -----
        var topSkills = skillRepository.findUserSkills(
                        userId,
                        PageRequests.of(0, 5, SkillSort.NAME_ASC.toSort())
                )
                .getContent()
                .stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getStatus().name()))
                .toList();

        MyProfileResponse base = userMapper.toMyProfileResponse(user);


        String profileUrl = (base.profileImageUrl() == null) ? null : storageService.signedUrl(base.profileImageUrl());


        int size = normalize(limit);
        long stamp = cacheStampService.getStamp(PROFILE_POSTS_STAMP);

        CursorPageResponse<PostCardCached> shared = profilePostsShared(stamp, userId, size, cursor);

        List<PostCardCached> baseItems = shared.items();
        if (baseItems.isEmpty()) {
            return new MyProfileWithPostsResponse(
                    base.id(),
                    base.fullName(),
                    base.email(),
                    base.phoneNumber(),
                    base.department(),
                    base.jobTitle(),
                    base.headline(),
                    profileUrl,
                    base.mentor(),
                    topSkills,
                    new CursorPageResponse<>(List.of(), shared.nextCursor())
            );
        }

        List<UUID> postIds = baseItems.stream().map(PostCardCached::id).toList();


        var likedRows = postLikeRepository.findLikedPostIds(userId, postIds);
        Set<UUID> likedSet = new HashSet<>();
        for (var r : likedRows) likedSet.add(r.getPostId());

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

        CursorPageResponse<PostCardResponse> postsPage =
                new CursorPageResponse<>(finalItems, shared.nextCursor());

        return new MyProfileWithPostsResponse(
                base.id(),
                base.fullName(),
                base.email(),
                base.phoneNumber(),
                base.department(),
                base.jobTitle(),
                base.headline(),
                profileUrl,
                base.mentor(),
                topSkills,
                postsPage
        );
    }
@Override
    @Transactional(readOnly = true)
    public MyProfileWithPostsResponse profileWithPosts(UUID viewerUserId, UUID profileUserId, Integer limit, String cursor) {

        User profile = userRepository.findById(profileUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        // top skills of PROFILE owner
        var topSkills = skillRepository.findUserSkills(
                        profileUserId,
                        PageRequests.of(0, 5, SkillSort.NAME_ASC.toSort())
                )
                .getContent().stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getStatus().name()))
                .toList();

        MyProfileResponse base = userMapper.toMyProfileResponse(profile);

        String profileUrl = (base.profileImageUrl() == null) ? null : storageService.signedUrl(base.profileImageUrl());

        // posts page (shared cached)
        int size = normalize(limit);
        long stamp = cacheStampService.getStamp(PROFILE_POSTS_STAMP);

        CursorPageResponse<PostCardCached> shared = profilePostsShared(stamp, profileUserId, size, cursor);

        List<PostCardCached> baseItems = shared.items();
        if (baseItems.isEmpty()) {
            return new MyProfileWithPostsResponse(
                    base.id(),
                    base.fullName(),
                    base.email(),
                    base.phoneNumber(),
                    base.department(),
                    base.jobTitle(),
                    base.headline(),
                    profileUrl,
                    base.mentor(),
                    topSkills,
                    new CursorPageResponse<>(List.of(), shared.nextCursor())
            );
        }

        List<UUID> postIds = baseItems.stream().map(PostCardCached::id).toList();

        // personalization: likedByMe depends on VIEWER
        var likedRows = postLikeRepository.findLikedPostIds(viewerUserId, postIds);
        Set<UUID> likedSet = new HashSet<>();
        for (var r : likedRows) likedSet.add(r.getPostId());

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

        CursorPageResponse<PostCardResponse> postsPage =
                new CursorPageResponse<>(finalItems, shared.nextCursor());

        return new MyProfileWithPostsResponse(
                base.id(),
                base.fullName(),
                base.email(),
                base.phoneNumber(),
                base.department(),
                base.jobTitle(),
                base.headline(),
                profileUrl,
                base.mentor(),
                topSkills,
                postsPage
        );
    }


    // ===================== Shared cached =====================

    @Cacheable(
            cacheNames = PROFILE_POSTS_CACHE,
            key = "T(com.company.skillplatform.common.cache.CacheKeys).profilePostsShared(#stamp, #profileUserId.toString(), #limit, #cursor)"
    )
    public CursorPageResponse<PostCardCached> profilePostsShared(long stamp, UUID profileUserId, int limit, String cursor) {
        return profilePostsSharedInternal(profileUserId, limit, cursor);
    }

    private CursorPageResponse<PostCardCached> profilePostsSharedInternal(UUID profileUserId, int limit, String cursor) {

        var pageable = PageRequest.of(
                0,
                limit + 1,
                Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id"))
        );

        List<Post> posts;
        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.profilePostsFirstPage(profileUserId, PostStatus.PUBLISHED, pageable);
        } else {
            PostCursor c = PostCursorCodec.decode(cursor);
            posts = postRepository.profilePostsNextPage(profileUserId, PostStatus.PUBLISHED, c.publishedAt(), c.id(), pageable);
        }

        boolean hasNext = posts.size() > limit;
        if (hasNext) posts = posts.subList(0, limit);

        if (posts.isEmpty()) {
            return new CursorPageResponse<>(List.of(), null);
        }

        List<UUID> postIds = posts.stream().map(Post::getId).toList();

        // likesCount batch
        var likeCounts = postLikeRepository.countByPostIds(postIds);
        Map<UUID, Long> likeCountMap = new HashMap<>();
        for (var row : likeCounts) likeCountMap.put(row.getPostId(), row.getCnt());

        // commentsCount batch
        var commentCounts = postCommentRepository.countActiveByPostIds(postIds);
        Map<UUID, Long> commentCountMap = new HashMap<>();
        for (var row : commentCounts) commentCountMap.put(row.getPostId(), row.getCnt());

        // attachments batch
        Map<UUID, List<PostAttachment>> attMap = groupAttachments(postIds);

        // sessions batch
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
        String authorUrl = (u.getProfileImageUrl() == null) ? null : storageService.signedUrl(u.getProfileImageUrl());

        PostCardResponse.AuthorCard authorCard = new PostCardResponse.AuthorCard(
                u.getId(),
                u.getFullName(),
                u.getDepartment(),
                u.getJobTitle(),
                u.getHeadline(),
                authorUrl
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

    @Transactional(readOnly = true)
    public CursorPageResponse<PostCardResponse> myDeletedPosts(
            UUID userId,
            Integer limit,
            String cursor
    ) {
        int size = normalize(limit);

        var pageable = PageRequest.of(
                0,
                size + 1,
                Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id"))
        );

        List<Post> posts;
        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.myDeletedFirstPage(
                    userId,
                    PostStatus.DELETED,
                    pageable
            );
        } else {
            PostCursor c = PostCursorCodec.decode(cursor);
            posts = postRepository.myDeletedNextPage(
                    userId,
                    PostStatus.DELETED,
                    c.publishedAt(),
                    c.id(),
                    pageable
            );
        }

        boolean hasNext = posts.size() > size;
        if (hasNext) posts = posts.subList(0, size);

        if (posts.isEmpty()) {
            return new CursorPageResponse<>(List.of(), null);
        }


        List<UUID> postIds = posts.stream().map(Post::getId).toList();

        var likeCounts = postLikeRepository.countByPostIds(postIds);
        var commentCounts = postCommentRepository.countActiveByPostIds(postIds);

        Map<UUID, Long> likeMap = toMap(likeCounts);
        Map<UUID, Long> commentMap = toMap(commentCounts);

        Map<UUID, List<PostAttachment>> attMap = groupAttachments(postIds);
        Map<UUID, Session> sessionMap = groupSessions(postIds);

        List<PostCardResponse> items = posts.stream()
                .map(p -> toPostCardResponse(
                        p,
                        likeMap,
                        commentMap,
                        attMap,
                        sessionMap
                ))
                .toList();

        String nextCursor = null;
        if (hasNext) {
            Post last = posts.get(posts.size() - 1);
            nextCursor = PostCursorCodec.encode(
                    new PostCursor(last.getPublishedAt(), last.getId())
            );
        }

        return new CursorPageResponse<>(items, nextCursor);
    }


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
        for (Session s : all) map.put(s.getPostId(), s);
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

    @Override
    public MyProfileResponse update(UUID userId, UpdateMyProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));


        String phone =Texts.clean(request.phoneNumber());
        String headline =(request.headline());
                if(headline!=null){headline.replaceAll("\\s{2,}", " ");}
        user.updateSelfFields(phone, headline);
        cacheStampService.bump(EmployeeDirectoryServiceImpl.EMP_DIR_STAMP);


        boolean wantsDepartmentChange = hasText(request.department()) && !request.department().trim().equalsIgnoreCase(user.getDepartment());
        boolean wantsJobTitleChange = hasText(request.jobTitle()) && !request.jobTitle().trim().equalsIgnoreCase(user.getJobTitle());

        if (wantsDepartmentChange || wantsJobTitleChange) {

            String requestedDepartment = wantsDepartmentChange ? request.department().trim() : null;
            String requestedJobTitle = wantsJobTitleChange ? request.jobTitle().trim() : null;


            String departmentContext = (requestedDepartment != null) ? requestedDepartment : user.getDepartment();

            String payloadJson = toJson(new ProfileChangePayload(
                    user.getDepartment(),
                    requestedDepartment,
                    user.getJobTitle(),
                    requestedJobTitle
            ));

            UUID approvalId = approvalRequestService.createOrUpdateProfileChangeRequest(
                    userId,
                    departmentContext,
                    payloadJson
            );

            log.info("Created/Updated PROFILE_CHANGE approval request id={} for userId={}", approvalId, userId);
        }

        log.info("Updated profile self-fields for userId={}", userId);
        var topSkills = skillRepository.findUserSkills(userId, PageRequests.of(0, 5, SkillSort.NAME_ASC.toSort()))
                .getContent()
                .stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getStatus().name()))
                .toList();

        MyProfileResponse base = userMapper.toMyProfileResponse(user);

        return new MyProfileResponse(
                base.id(),
                base.fullName(),
                base.email(),
                base.phoneNumber(),
                base.department(),
                base.jobTitle(),
                base.headline(),
                base.profileImageUrl(),
                base.mentor(),
                topSkills
        );

    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize approval payload", e);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isBlank();
    }
    private Map<UUID, Long> toMap(List<? extends Object> rows) {
        Map<UUID, Long> map = new HashMap<>();
        for (var r : rows) {

            try {
                UUID postId = (UUID) r.getClass().getMethod("getPostId").invoke(r);
                Long cnt = (Long) r.getClass().getMethod("getCnt").invoke(r);
                map.put(postId, cnt);
            } catch (Exception e) {
                throw new IllegalStateException("Invalid count row projection", e);
            }
        }
        return map;
    }
    private PostCardResponse toPostCardResponse(
            Post p,
            Map<UUID, Long> likeMap,
            Map<UUID, Long> commentMap,
            Map<UUID, List<PostAttachment>> attMap,
            Map<UUID, Session> sessionMap
    ) {
        String bodyPreview = preview(p.getBody(), 220);

        var u = p.getAuthor();
        String authorUrl = (u.getProfileImageUrl() == null)
                ? null
                : storageService.signedUrl(u.getProfileImageUrl());

        PostCardResponse.AuthorCard authorCard =
                new PostCardResponse.AuthorCard(
                        u.getId(),
                        u.getFullName(),
                        u.getDepartment(),
                        u.getJobTitle(),
                        u.getHeadline(),
                        authorUrl
                );

        List<PostCardResponse.AttachmentMini> minis =
                attMap.getOrDefault(p.getId(), List.of())
                        .stream()
                        .limit(3)
                        .map(a -> new PostCardResponse.AttachmentMini(
                                a.getId(),
                                a.getType().name(),
                                storageService.signedUrl(a.getStorageKey())
                        ))
                        .toList();

        PostCardResponse.SessionMini sessionMini = null;
        Session session = sessionMap.get(p.getId());
        if (session != null) {
            sessionMini = new PostCardResponse.SessionMini(
                    session.getId(),
                    session.getTitle(),
                    session.getStartsAt(),
                    session.getDurationMinutes(),
                    session.getStatus().name()
            );
        }

        return new PostCardResponse(
                p.getId(),
                p.getTitle(),
                bodyPreview,
                p.getPublishedAt(),
                authorCard,
                likeMap.getOrDefault(p.getId(), 0L),
                false,
                commentMap.getOrDefault(p.getId(), 0L),
                minis,
                sessionMini
        );
    }


}

