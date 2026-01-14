package com.company.skillplatform.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddSkillRequest(
        @NotBlank(message = "Skill name is required")
        @Size(max = 80, message = "Skill name must not exceed 80 characters")
        String name
) {}

