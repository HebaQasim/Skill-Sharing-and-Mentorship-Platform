package com.company.skillplatform.admin.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeUserRoleRequest(
        @NotNull UUID userId,
        @NotNull TargetRole targetRole
) {
    public enum TargetRole {
        EMPLOYEE,
        DEPARTMENT_ADMIN
    }
}

