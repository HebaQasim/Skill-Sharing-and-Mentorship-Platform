package com.company.skillplatform.approval.dto;

import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MyApprovalRequestItemResponse(
        UUID id,
        ApprovalRequestType type,
        ApprovalStatus status,
        String departmentContext,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        String reviewNote
) {}
