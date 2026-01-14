package com.company.skillplatform.user.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.PostCardResponse;
import com.company.skillplatform.user.dto.MyProfileResponse;
import com.company.skillplatform.user.dto.MyProfileWithPostsResponse;
import com.company.skillplatform.user.dto.UpdateMyProfileRequest;

import java.util.UUID;

public interface MyProfileService {
    MyProfileWithPostsResponse meWithPosts(UUID userId, Integer limit, String cursor);

    MyProfileResponse update(UUID userId, UpdateMyProfileRequest request);
    MyProfileWithPostsResponse profileWithPosts(UUID viewerUserId, UUID profileUserId, Integer limit, String cursor) ;
    CursorPageResponse<PostCardResponse> myDeletedPosts(
            UUID userId,
            Integer limit,
            String cursor
    );
    }
