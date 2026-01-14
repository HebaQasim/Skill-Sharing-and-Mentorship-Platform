package com.company.skillplatform.post.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.PostCardResponse;
import com.company.skillplatform.post.dto.PostDetailsResponse;

import java.util.UUID;

public interface PostFeedService {
    CursorPageResponse<PostCardResponse> feed(UUID userId, Integer limit, String cursorToken);

    PostDetailsResponse getPost(UUID viewerUserId,UUID postId);
}
