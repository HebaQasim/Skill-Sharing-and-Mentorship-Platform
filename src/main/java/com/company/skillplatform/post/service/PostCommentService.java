package com.company.skillplatform.post.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.*;

import java.util.UUID;

public interface PostCommentService {
    CommentResponse add(UUID userId, UUID postId, CreateCommentRequest request);
    CommentResponse edit(UUID userId, UUID postId, UUID commentId, EditCommentRequest request);
    void delete(UUID userId, UUID postId, UUID commentId);

    CursorPageResponse<CommentResponse> list(UUID userId, UUID postId, Integer limit, String cursor);
}

