package com.company.skillplatform.post.repository;

import java.util.UUID;

public interface LikeCountRow {
    UUID getPostId();
    long getCnt();
}
