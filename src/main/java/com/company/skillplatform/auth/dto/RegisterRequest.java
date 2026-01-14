package com.company.skillplatform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @NotBlank(message="Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
                message = "Password must contain upper, lower, number, and special character"
        )
        String password,

        @NotBlank(message = "Department is required")
        String department,

        @NotBlank(message = "Job title is required")
        String jobTitle,

        @Pattern(
                regexp = "^[0-9+\\- ]{7,20}$",
                message = "Invalid phone number"
        )
        String phoneNumber
) {
}

