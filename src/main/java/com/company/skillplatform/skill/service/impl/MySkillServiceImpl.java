package com.company.skillplatform.skill.service.impl;

import com.company.skillplatform.approval.entity.ApprovalRequest;
import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;
import com.company.skillplatform.approval.event.ApprovalRequestedEvent;
import com.company.skillplatform.approval.payload.ProfileChangePayload;
import com.company.skillplatform.approval.payload.SkillCreatePayload;
import com.company.skillplatform.approval.repository.ApprovalRequestRepository;
import com.company.skillplatform.approval.service.ApprovalRequestService;
import com.company.skillplatform.approval.service.ApprovalRoutingService;
import com.company.skillplatform.common.exception.BusinessException;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.skill.dto.SkillResponse;
import com.company.skillplatform.skill.entity.Skill;
import com.company.skillplatform.skill.enums.SkillStatus;
import com.company.skillplatform.skill.repository.SkillRepository;
import com.company.skillplatform.skill.service.MySkillService;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MySkillServiceImpl implements MySkillService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    private final ApprovalRequestRepository approvalRequestRepository;
    private final ApprovalRoutingService routingService;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final ApprovalRequestService approvalRequestService;
    @Override
    public SkillResponse addSkill(UUID userId, String rawSkillName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        String normalized = Skill.normalizeName(rawSkillName);
        if (normalized == null) {
            throw new IllegalArgumentException("Skill name is empty");
        }

        Skill existing = skillRepository.findByNameIgnoreCase(normalized).orElse(null);
        if (existing != null && existing.getStatus() == SkillStatus.REJECTED) {
            throw new BusinessException(ErrorCode.SKILL_REJECTED, "This skill name was rejected. Please choose a different name.");
        }
        // 1) If ACTIVE -> attach immediately
        if (existing != null && existing.getStatus() == SkillStatus.ACTIVE) {
            boolean alreadyAdded = user.getSkills().stream().anyMatch(s -> s.getId().equals(existing.getId()));
            if (!alreadyAdded) user.addSkill(existing);
            return new SkillResponse(existing.getId(), existing.getName(), existing.getStatus().name());
        }

        // 2) If exists but not ACTIVE -> ensure approval request exists
        if (existing != null) {
            String payloadJson = toJson(new SkillCreatePayload(
                    existing.getId(),normalized));
            approvalRequestService.createOrUpdateSkillCreateRequest(
                    userId,
                    user.getDepartment(),
                    payloadJson
            );
            return new SkillResponse(existing.getId(), existing.getName(), existing.getStatus().name());
        }


        Skill pending = skillRepository.save(Skill.builder()
                .name(normalized)
                .status(SkillStatus.PENDING)
                .requestedByUserId(userId)
                .build());



        String payloadJson = toJson(new SkillCreatePayload(
                pending.getId(),normalized));

        UUID approvalId = approvalRequestService.createOrUpdateSkillCreateRequest(
                userId,
                user.getDepartment(),
                payloadJson
        );

        log.info("Created/Updated PROFILE_CHANGE approval request id={} for userId={}", approvalId, userId);



        return new SkillResponse(pending.getId(), pending.getName(), pending.getStatus().name());
    }

    @Override
    public void removeSkill(UUID userId, UUID skillId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SKILL_NOT_FOUND, "Skill not found"));

        user.removeSkill(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<SkillResponse> searchActiveSkills(String q) {
        String query = (q == null) ? "" : q.trim();
        return skillRepository
                .findTop20ByStatusAndNameContainingIgnoreCaseOrderByNameAsc(SkillStatus.ACTIVE, query)
                .stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getStatus().name()))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public Page<SkillResponse> mySkills(UUID userId, Pageable pageable) {
        return skillRepository.findUserSkills(userId, pageable)
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getStatus().name()));
    }



    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
    }
}
