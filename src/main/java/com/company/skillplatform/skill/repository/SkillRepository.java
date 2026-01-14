package com.company.skillplatform.skill.repository;

import com.company.skillplatform.skill.entity.Skill;
import com.company.skillplatform.skill.enums.SkillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findById(UUID id);

    @Query("""
    select s
    from User u
    join u.skills s
    where u.id = :userId
""")
    Page<Skill> findUserSkills(@Param("userId") UUID userId, Pageable pageable);
    Optional<Skill> findByNameIgnoreCase(String name);

    List<Skill> findTop20ByStatusAndNameContainingIgnoreCaseOrderByNameAsc(SkillStatus status, String q);
}
