package com.company.skillplatform.session.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFeedbackRequest(
        @Min(1) @Max(5)
        Integer rating,

        @NotBlank
        @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
        String comment
) {}

