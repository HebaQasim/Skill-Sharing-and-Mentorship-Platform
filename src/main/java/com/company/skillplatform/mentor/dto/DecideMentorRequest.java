package com.company.skillplatform.mentor.dto;

import jakarta.validation.constraints.Size;

public record DecideMentorRequest(
        @Size(max = 500) String note
) {}
