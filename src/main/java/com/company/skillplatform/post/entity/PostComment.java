package com.company.skillplatform.post.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "post_comments",
        indexes = {
                @Index(name = "idx_comments_post_created", columnList = "postId,createdAt,id"),
                @Index(name = "idx_comments_author", columnList = "authorUserId,createdAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostComment extends BaseEntity {

    @Column(nullable = false)
    private UUID postId;

    @Column(nullable = false)
    private UUID authorUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorUserId", insertable = false, updatable = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String body;

    private LocalDateTime editedAt;

    private LocalDateTime deletedAt;

    public boolean isOwner(UUID userId) {
        return authorUserId.equals(userId);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void edit(String newBody) {
        if (isDeleted()) throw new IllegalStateException("Deleted comment cannot be edited");
        this.body = newBody;
        this.editedAt = LocalDateTime.now();
    }

    public void softDelete() {
        if (!isDeleted()) {
            this.deletedAt = LocalDateTime.now();
        }
    }
}

