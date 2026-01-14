package com.company.skillplatform.post.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.DraftPostListItemResponse;
import com.company.skillplatform.post.dto.PostDraftResponse;
import com.company.skillplatform.post.dto.UpdatePostDraftRequest;

import java.util.UUID;

public interface PostDraftService {

    PostDraftResponse createDraft(UUID userId);

    PostDraftResponse getDraft(UUID userId, UUID postId);

    PostDraftResponse updateDraft(UUID userId, UUID postId, UpdatePostDraftRequest request);

    void publish(UUID userId, UUID postId);
    CursorPageResponse<DraftPostListItemResponse> myDrafts(UUID userId, Integer limit, String cursor);
}


