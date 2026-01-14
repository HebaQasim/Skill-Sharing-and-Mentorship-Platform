package com.company.skillplatform.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectApprovalRequest(
        @NotBlank(message = "Rejection note is required")
        @Size(max = 500, message = "Note must not exceed 500 characters")
        String note
) {}

