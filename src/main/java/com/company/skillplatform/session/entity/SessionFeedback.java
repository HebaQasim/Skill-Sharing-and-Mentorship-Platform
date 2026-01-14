package com.company.skillplatform.session.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "session_feedback",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_feedback_session_author",
                columnNames = {"sessionId", "authorUserId"}
        ),
        indexes = {
                @Index(name = "idx_feedback_session_created", columnList = "sessionId,createdAt,id"),
                @Index(name = "idx_feedback_author", columnList = "authorUserId,createdAt")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionFeedback extends BaseEntity {

    @Column(nullable = false)
    private UUID sessionId;

    @Column(nullable = false)
    private UUID authorUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorUserId", insertable = false, updatable = false)
    private User author;

    // optional: rating 1..5
    private Integer rating;

    @Column(nullable = false, length = 1000)
    private String comment;

    private LocalDateTime editedAt;

    private LocalDateTime deletedAt;

    public boolean isOwner(UUID userId) {
        return authorUserId.equals(userId);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void edit(Integer rating, String comment) {
        if (isDeleted()) throw new IllegalStateException("Deleted feedback cannot be edited");
        this.rating = rating;
        this.comment = comment;
        this.editedAt = LocalDateTime.now();
    }

    public void softDelete() {
        if (!isDeleted()) {
            this.deletedAt = LocalDateTime.now();
        }
    }
}
