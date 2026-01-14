package com.company.skillplatform.approval.dto;

import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminApprovalRequestItemResponse(
        UUID id,
        ApprovalRequestType type,
        ApprovalStatus status,
        UUID requestedByUserId,
        String departmentContext,
        LocalDateTime createdAt
) {}
