package com.company.skillplatform.approval.payload;

import java.util.UUID;

public record SkillCreatePayload(UUID skillId, String skillName) {
}
