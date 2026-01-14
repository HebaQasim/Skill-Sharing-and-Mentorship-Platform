package com.company.skillplatform.approval.payload;

public record ProfileChangePayload(
        String currentDepartment,
        String requestedDepartment,
        String currentJobTitle,
        String requestedJobTitle
) {}

