package com.company.skillplatform.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditCommentRequest(
        @NotBlank
        @Size(max = 1000, message = "Comment must not exceed 1000 characters")
        String body
) {}

