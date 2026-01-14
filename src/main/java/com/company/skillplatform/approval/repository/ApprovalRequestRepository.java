package com.company.skillplatform.approval.repository;

import com.company.skillplatform.approval.entity.ApprovalRequest;
import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {

    Optional<ApprovalRequest> findByRequestedByUserIdAndTypeAndStatus(
            UUID requestedByUserId,
            ApprovalRequestType type,
            ApprovalStatus status
    );

    Optional<ApprovalRequest> findByRequestKeyAndStatus(String requestKey, ApprovalStatus status);


    Page<ApprovalRequest> findByAssignedToUserIdAndStatus(
            UUID assignedToUserId,
            ApprovalStatus status,
            Pageable pageable
    );

    //  My requests (no filters)
    Page<ApprovalRequest> findByRequestedByUserId(UUID userId, Pageable pageable);

    //  My requests (filter by status)
    Page<ApprovalRequest> findByRequestedByUserIdAndStatus(UUID userId, ApprovalStatus status, Pageable pageable);

    // My requests (filter by type)
    Page<ApprovalRequest> findByRequestedByUserIdAndType(UUID userId, ApprovalRequestType type, Pageable pageable);

    // My requests (filter by status + type)
    Page<ApprovalRequest> findByRequestedByUserIdAndStatusAndType(
            UUID userId,
            ApprovalStatus status,
            ApprovalRequestType type,
            Pageable pageable
    );
    Optional<ApprovalRequest> findByIdAndRequestedByUserId(UUID id, UUID requestedByUserId);
    Optional<ApprovalRequest> findByIdAndAssignedToUserId(UUID id, UUID assignedToUserId);
    Page<ApprovalRequest> findByAssignedToUserIdAndStatusAndType(
            UUID assignedToUserId,
            ApprovalStatus status,
            ApprovalRequestType type,
            Pageable pageable
    );
    Page<ApprovalRequest> findByAssignedToUserIdAndType(UUID assignedToUserId, ApprovalRequestType type, Pageable pageable);
    Page<ApprovalRequest> findByAssignedToUserId(UUID assignedToUserId, Pageable pageable);
}

