package com.company.skillplatform.approval.service.impl;

import com.company.skillplatform.approval.dto.MyApprovalFilter;
import com.company.skillplatform.approval.dto.MyApprovalRequestDetailsResponse;
import com.company.skillplatform.approval.entity.ApprovalRequest;
import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;
import com.company.skillplatform.approval.event.ApprovalRequestedEvent;
import com.company.skillplatform.approval.repository.ApprovalRequestRepository;
import com.company.skillplatform.approval.service.ApprovalRequestService;
import com.company.skillplatform.approval.service.ApprovalRoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalRequestServiceImpl implements ApprovalRequestService {

    private final ApprovalRequestRepository repository;
    private final ApprovalRoutingService routingService;
    private final ApplicationEventPublisher publisher;





    @Override
    public UUID createOrUpdateProfileChangeRequest(UUID requesterId, String departmentContext, String payloadJson) {

        UUID assignedTo = routingService.resolveApproverForDepartment(departmentContext);
        String requestKey = "PROFILE_CHANGE:" + requesterId;

        var existing = repository.findByRequestKeyAndStatus(requestKey, ApprovalStatus.PENDING);



        ApprovalRequest request;
        if (existing.isPresent()) {
            request = existing.get();
            request.updatePending(payloadJson, departmentContext, assignedTo);
        } else {
            request = ApprovalRequest.builder()
                    .requestKey(requestKey)
                    .type(ApprovalRequestType.PROFILE_CHANGE)
                    .status(ApprovalStatus.PENDING)
                    .requestedByUserId(requesterId)
                    .assignedToUserId(assignedTo)
                    .departmentContext(departmentContext)
                    .payload(payloadJson)
                    .build();

        }

        ApprovalRequest saved = repository.save(request);


        publisher.publishEvent(new ApprovalRequestedEvent(saved.getId()));

        return saved.getId();
    }

    @Override
    public UUID createOrUpdateSkillCreateRequest(UUID requesterId, String departmentContext, String payloadJson) {
        UUID assignedTo = routingService.resolveApproverForDepartment(departmentContext);
        String requestKey = "SKILL_CREATE:" + requesterId;

        var existing = repository.findByRequestKeyAndStatus(requestKey, ApprovalStatus.PENDING);

        ApprovalRequest req;
        if (existing.isPresent()) {
            req = existing.get();
            req.updatePending(payloadJson, departmentContext, assignedTo);
        } else {
            req = ApprovalRequest.builder()
                    .requestKey(requestKey)
                    .type(ApprovalRequestType.SKILL_CREATE)
                    .status(ApprovalStatus.PENDING)
                    .requestedByUserId(requesterId)
                    .assignedToUserId(assignedTo)
                    .departmentContext(departmentContext)
                    .payload(payloadJson)
                    .build();
        }

        var saved = repository.save(req);
        publisher.publishEvent(new ApprovalRequestedEvent(saved.getId()));
        return saved.getId();
    }

}
