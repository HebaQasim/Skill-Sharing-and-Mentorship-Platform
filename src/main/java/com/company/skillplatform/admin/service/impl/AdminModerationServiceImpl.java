package com.company.skillplatform.admin.service.impl;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.post.entity.Post;
import com.company.skillplatform.post.entity.PostComment;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.post.repository.PostCommentRepository;
import com.company.skillplatform.post.repository.PostRepository;
import com.company.skillplatform.session.entity.SessionFeedback;
import com.company.skillplatform.session.repository.SessionFeedbackRepository;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.admin.service.AdminModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminModerationServiceImpl implements AdminModerationService {

    public static final String FEED_STAMP_NAME = "postFeed";

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final SessionFeedbackRepository sessionFeedbackRepository;
    private final UserRepository userRepository;
    private final CacheStampService cacheStampService;

    @Override
    public void deletePost(UserPrincipal moderator, UUID postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "Post not found"));


        enforceDepartmentScope(moderator, post.getAuthorUserId());


        if (post.getStatus() == PostStatus.DELETED) return;
        post.markDeleted();

        cacheStampService.bump(FEED_STAMP_NAME);
    }

    @Override
    public void deleteComment(UserPrincipal moderator, UUID postId, UUID commentId) {


        PostComment c = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COMMENT_NOT_FOUND, "Comment not found"));


        if (!c.getPostId().equals(postId)) {
            throw new ResourceNotFoundException(ErrorCode.COMMENT_NOT_FOUND, "Comment not found");
        }

        enforceDepartmentScope(moderator, c.getAuthorUserId());

        postCommentRepository.deleteById(commentId);

        cacheStampService.bump(FEED_STAMP_NAME);
    }

    @Override
    public void deleteFeedback(UserPrincipal moderator, UUID sessionId, UUID feedbackId) {

        SessionFeedback f = sessionFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, "Feedback not found"));


        if (!f.getSessionId().equals(sessionId)) {
            throw new ResourceNotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, "Feedback not found");
        }

        enforceDepartmentScope(moderator, f.getAuthorUserId());

        sessionFeedbackRepository.deleteById(feedbackId);

        cacheStampService.bump(FEED_STAMP_NAME);
    }

    // ---------------- helpers ----------------

    private void enforceDepartmentScope(UserPrincipal moderator, UUID contentOwnerUserId) {

        if (hasRole(moderator, "ADMIN")) return;


        if (!hasRole(moderator, "DEPARTMENT_ADMIN")) {
            throw new ForbiddenException("Not allowed");
        }

        User mod = userRepository.findById(moderator.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        User owner = userRepository.findById(contentOwnerUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String modDept = safe(mod.getDepartment());
        String ownerDept = safe(owner.getDepartment());

        if (!modDept.equals(ownerDept)) {
            throw new ForbiddenException("Not allowed");
        }
    }

    private boolean hasRole(UserPrincipal p, String role) {
        return p.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private String safe(String v) {
        return (v == null) ? "" : v.trim();
    }
}
