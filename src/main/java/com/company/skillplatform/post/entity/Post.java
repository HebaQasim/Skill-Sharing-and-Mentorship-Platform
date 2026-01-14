package com.company.skillplatform.post.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.post.enums.PostStatus;
import com.company.skillplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_posts_status_published", columnList = "status,publishedAt,id"),
                @Index(name = "idx_posts_author_published", columnList = "authorUserId,publishedAt,id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Column(nullable = false)
    private UUID authorUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorUserId", insertable = false, updatable = false)
    private User author;

    @Column(length = 200)
    private String title;

    @Lob
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    private LocalDateTime publishedAt;
    private LocalDateTime editedAt;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostAttachment> attachments;

    public boolean isOwner(UUID userId) {
        return authorUserId.equals(userId);
    }

    public boolean isDraft() {
        return this.status == PostStatus.DRAFT;
    }

    public boolean isPublished() {
        return this.status == PostStatus.PUBLISHED;
    }

    public void edit(String title, String body)
    {
        if(title!=null){
            this.title=title;
        }
        if(body!=null){
            this.body=body;
        }
        this.editedAt = LocalDateTime.now();
    }

    public void publish() {
        this.status = PostStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.editedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.status = PostStatus.DELETED;
        this.editedAt = LocalDateTime.now();
    }

    public void restorePublished() {
        this.status = PostStatus.PUBLISHED;
    }
}
