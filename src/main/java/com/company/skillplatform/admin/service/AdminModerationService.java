package com.company.skillplatform.admin.service;

import com.company.skillplatform.auth.security.UserPrincipal;

import java.util.UUID;

public interface AdminModerationService {
    void deletePost(UserPrincipal moderator, UUID postId);
    void deleteComment(UserPrincipal moderator, UUID postId, UUID commentId);
    void deleteFeedback(UserPrincipal moderator, UUID sessionId, UUID feedbackId);

}
