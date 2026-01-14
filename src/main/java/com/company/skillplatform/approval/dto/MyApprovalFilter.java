package com.company.skillplatform.approval.dto;

import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;

public record MyApprovalFilter(
        ApprovalStatus status,
        ApprovalRequestType type
) {}
