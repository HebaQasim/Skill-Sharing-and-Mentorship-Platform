package com.company.skillplatform.approval.dto;

import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminApprovalRequestDetailsResponse(
        UUID id,
        ApprovalRequestType type,
        ApprovalStatus status,
        UUID requestedByUserId,
        UUID assignedToUserId,
        String departmentContext,
        String payload,
        String requestKey,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        UUID reviewedByUserId,
        String reviewNote
) {}

