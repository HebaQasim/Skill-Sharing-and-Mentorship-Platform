package com.company.skillplatform.mentor.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.mentor.enums.MentorRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mentor_badge_requests",
        uniqueConstraints = @UniqueConstraint(name="uk_mentor_req_pending", columnNames={"user_id","status"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MentorBadgeRequest {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;


    @Column(name = "threshold_count", nullable = false)
    private long thresholdCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MentorRequestStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;

    @Column(columnDefinition = "BINARY(16)")
    private UUID decidedByAdminId;

    @Column(length = 500)
    private String decisionNote;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = MentorRequestStatus.PENDING;
    }

    public void approve(UUID adminId, String note) {
        this.status = MentorRequestStatus.APPROVED;
        this.decidedAt = LocalDateTime.now();
        this.decidedByAdminId = adminId;
        this.decisionNote = note;
    }

    public void reject(UUID adminId, String note) {
        this.status = MentorRequestStatus.REJECTED;
        this.decidedAt = LocalDateTime.now();
        this.decidedByAdminId = adminId;
        this.decisionNote = note;
    }
}
