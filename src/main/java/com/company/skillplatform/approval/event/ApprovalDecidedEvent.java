package com.company.skillplatform.approval.event;

import com.company.skillplatform.approval.enums.ApprovalStatus;
import java.util.UUID;

public record ApprovalDecidedEvent(UUID approvalRequestId, ApprovalStatus status) {}

