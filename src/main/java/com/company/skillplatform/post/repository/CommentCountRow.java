package com.company.skillplatform.post.repository;

import java.util.UUID;

public interface CommentCountRow {
    UUID getPostId();
    long getCnt();
}
