package com.company.skillplatform.post.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.PostLikerResponse;

import java.util.UUID;

public interface PostLikeService {
    void like(UUID userId, UUID postId);
    void unlike(UUID userId, UUID postId);
    CursorPageResponse<PostLikerResponse> likers(UUID postId, Integer limit, String cursor);
}

