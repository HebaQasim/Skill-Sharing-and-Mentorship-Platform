package com.company.skillplatform.skill.dto;

import java.util.UUID;

public record SkillResponse(
        UUID id,
        String name,
        String status
) {}
