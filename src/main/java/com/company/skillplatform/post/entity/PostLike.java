package com.company.skillplatform.post.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_like", columnNames = {"postId", "userId"}),
        indexes = {
                @Index(name = "idx_post_likes_post", columnList = "postId,createdAt"),
                @Index(name = "idx_post_likes_user", columnList = "userId,createdAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostLike extends BaseEntity {

    @Column(nullable = false)
    private UUID postId;

    @Column(nullable = false)
    private UUID userId;
}

