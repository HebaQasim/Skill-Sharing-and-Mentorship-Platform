package com.company.skillplatform.approval.listener;

import com.company.skillplatform.approval.entity.ApprovalRequest;
import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;
import com.company.skillplatform.approval.event.ApprovalDecidedEvent;
import com.company.skillplatform.approval.event.ApprovalFinishedEvent;
import com.company.skillplatform.approval.payload.ProfileChangePayload;
import com.company.skillplatform.approval.payload.SkillCreatePayload;
import com.company.skillplatform.approval.repository.ApprovalRequestRepository;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.common.util.Texts;
import com.company.skillplatform.skill.entity.Skill;
import com.company.skillplatform.skill.enums.SkillStatus;
import com.company.skillplatform.skill.repository.SkillRepository;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.user.service.impl.EmployeeDirectoryServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprovalApplyListener {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final SkillRepository skillRepository;
    private final ApplicationEventPublisher publisher;
    private final CacheStampService cacheStampService;
    @EventListener
    @Transactional
    public void on(ApprovalDecidedEvent event) {

        // Apply changes only on APPROVED
        if (event.status() != ApprovalStatus.APPROVED) {
            return;
        }

        ApprovalRequest req = approvalRequestRepository.findById(event.approvalRequestId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.APPROVAL_REQUEST_NOT_FOUND,
                        "Approval request not found"
                ));


        if (req.getStatus() != ApprovalStatus.APPROVED) {
            return;
        }

        if (req.getType() == ApprovalRequestType.PROFILE_CHANGE) {
            applyProfileChange(req);
            return;
        }
        if (req.getType() == ApprovalRequestType.SKILL_CREATE) {
            applySkillCreate(req);
            return;
        }


        log.info("No apply handler yet for type={} approvalId={}", req.getType(), req.getId());
    }

    private void applyProfileChange(ApprovalRequest req) {
        ProfileChangePayload payload = readPayload(req.getPayload(), ProfileChangePayload.class, req.getId());

        User user = userRepository.findById(req.getRequestedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "User not found"
                ));

        String requestedDepartment = Texts.clean(payload.requestedDepartment());
        String requestedJobTitle = Texts.clean(payload.requestedJobTitle());

        user.applyApprovedProfileChange(requestedDepartment, requestedJobTitle);
        publisher.publishEvent(new ApprovalFinishedEvent(req.getId(), req.getStatus()));
        cacheStampService.bump(EmployeeDirectoryServiceImpl.EMP_DIR_STAMP);

        log.info("Applied PROFILE_CHANGE approvalId={} userId={} dept={} jobTitle={}",
                req.getId(), user.getId(), requestedDepartment, requestedJobTitle);
    }
    private void applySkillCreate(ApprovalRequest req) {
        SkillCreatePayload payload = readPayload(req.getPayload(), SkillCreatePayload.class, req.getId());

        Skill skill = skillRepository.findById(payload.skillId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SKILL_NOT_FOUND,
                        "Skill not found"
                ));


        if (skill.getStatus() == SkillStatus.PENDING) {
            skill.activate();
        }

        User user = userRepository.findById(req.getRequestedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "User not found"
                ));


        user.addSkill(skill);
        publisher.publishEvent(new ApprovalFinishedEvent(req.getId(), req.getStatus()));
        cacheStampService.bump(EmployeeDirectoryServiceImpl.EMP_DIR_STAMP);

        log.info("Applied SKILL_CREATE approvalId={} skillId={} userId={}",
                req.getId(), skill.getId(), user.getId());
    }

    private <T> T readPayload(String json, Class<T> type, java.util.UUID approvalId) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid approval payload approvalId=" + approvalId, e);
        }
    }
}
