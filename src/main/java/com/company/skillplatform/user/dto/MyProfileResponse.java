package com.company.skillplatform.user.dto;

import com.company.skillplatform.skill.dto.SkillResponse;

import java.util.List;
import java.util.UUID;

public record MyProfileResponse(
        UUID id,
        String fullName,
        String email,
        String phoneNumber,
        String department,
        String jobTitle,
        String headline,
        String profileImageUrl,
        boolean mentor,
        List<SkillResponse>skills
) {}

