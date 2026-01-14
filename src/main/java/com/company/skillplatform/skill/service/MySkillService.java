package com.company.skillplatform.skill.service;

import com.company.skillplatform.skill.dto.SkillResponse;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface MySkillService {
    SkillResponse addSkill(UUID userId, String rawSkillName);
    void removeSkill(UUID userId, UUID skillId);
    List<SkillResponse> searchActiveSkills(String q);
    Page<SkillResponse> mySkills(UUID userId, Pageable pageable);

}
