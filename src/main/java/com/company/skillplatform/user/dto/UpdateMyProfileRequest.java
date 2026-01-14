package com.company.skillplatform.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(

        @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "Invalid phone number")
        String phoneNumber,

        @Size(max = 220, message = "Headline must not exceed 220 characters")
        String headline,

        @Size(max = 100, message = "Job title must not exceed 100 characters")
        String jobTitle,

        @Size(max = 100, message = "Department must not exceed 100 characters")
        String department
) {}

