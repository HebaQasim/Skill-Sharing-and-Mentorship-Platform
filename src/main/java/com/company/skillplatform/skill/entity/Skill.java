package com.company.skillplatform.skill.entity;

import com.company.skillplatform.common.entity.BaseEntity;
import com.company.skillplatform.skill.enums.SkillStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "skills",
        uniqueConstraints = @UniqueConstraint(name = "uk_skills_name", columnNames = "name"),
        indexes = {
                @Index(name = "idx_skills_name", columnList = "name"),
                @Index(name = "idx_skills_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill extends BaseEntity {

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillStatus status;

    // Who requested this skill creation (audit)
    private UUID requestedByUserId;

    public void activate() {
        this.status = SkillStatus.ACTIVE;
    }

    public void reject() {
        this.status = SkillStatus.REJECTED;
    }

    public static String normalizeName(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isBlank()) return null;

        // Keep emojis/symbols, just normalize whitespace
        return trimmed.replaceAll("\\s{2,}", " ");
    }
}

