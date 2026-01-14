package com.company.skillplatform.post.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.post.enums.AttachmentType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "post_attachments",
        indexes = {
                @Index(name = "idx_post_attachments_post_created", columnList = "postId,createdAt,id"),
                @Index(name = "idx_post_attachments_uploader", columnList = "uploadedByUserId")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostAttachment extends BaseEntity {

    @Column(nullable = false)
    private UUID postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postId", insertable = false, updatable = false)
    private Post post;

    @Column(nullable = false)
    private UUID uploadedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttachmentType type;

    @Column(nullable = false, length = 200)
    private String originalFilename;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 300, unique = true)
    private String storageKey;
}
