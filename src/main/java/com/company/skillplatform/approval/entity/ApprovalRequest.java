package com.company.skillplatform.approval.entity;

import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;
import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(
        name = "approval_requests",
        indexes = {
                @Index(name = "idx_approval_status", columnList = "status"),
                @Index(name = "idx_approval_department", columnList = "departmentContext"),
                @Index(name = "idx_approval_requested_by", columnList = "requestedByUserId"),
                @Index(name = "idx_approval_request_key", columnList = "requestKey"),
                @Index(name = "idx_approval_request_key_status", columnList = "requestKey,status")
        }

)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequest extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;


    @Column(nullable = false)
    private UUID requestedByUserId;


    @Column(nullable = false)
    private UUID assignedToUserId;


    @Column(nullable = false, length = 100)
    private String departmentContext;


    @Lob
    @Column(nullable = false)
    private String payload;


    private UUID reviewedByUserId;
    private LocalDateTime reviewedAt;

    @Column(length = 500)
    private String reviewNote;

    public void approve(UUID reviewerId) {
        this.status = ApprovalStatus.APPROVED;
        this.reviewedByUserId = reviewerId;
        this.reviewedAt = LocalDateTime.now();
    }

    @Column(nullable = false, length = 120)
    private String requestKey;


    public void reject(UUID reviewerId, String note) {
        this.status = ApprovalStatus.REJECTED;
        this.reviewedByUserId = reviewerId;
        this.reviewNote = note;
        this.reviewedAt = LocalDateTime.now();
    }

    public void cancel(UUID requesterId) {
        if (!this.requestedByUserId.equals(requesterId)) return;
        this.status = ApprovalStatus.CANCELED;
    }
    public void updatePending(String newPayload, String newDepartmentContext, UUID newAssignedToUserId) {
        if (this.status != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be updated");
        }
        this.payload = newPayload;
        this.departmentContext = newDepartmentContext;
        this.assignedToUserId = newAssignedToUserId;
    }
    public boolean isAssignedTo(UUID reviewerId) {
        return this.assignedToUserId.equals(reviewerId);
    }

}

