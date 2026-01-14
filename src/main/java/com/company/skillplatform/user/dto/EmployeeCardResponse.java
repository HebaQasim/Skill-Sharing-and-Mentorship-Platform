package com.company.skillplatform.user.dto;

import java.util.UUID;

public record EmployeeCardResponse(
        UUID id,
        String fullName,
        String department,
        String jobTitle,
        String headline,
        String profileImageUrl
) {}
