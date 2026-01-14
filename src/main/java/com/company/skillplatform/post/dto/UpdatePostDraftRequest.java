package com.company.skillplatform.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UpdatePostDraftRequest(
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
        String body
) {}
