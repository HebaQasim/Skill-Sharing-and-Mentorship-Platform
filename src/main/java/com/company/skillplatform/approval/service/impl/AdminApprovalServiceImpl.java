package com.company.skillplatform.approval.service.impl;

import com.company.skillplatform.approval.dto.*;
import com.company.skillplatform.approval.entity.ApprovalRequest;
import com.company.skillplatform.approval.enums.*;
import com.company.skillplatform.approval.event.ApprovalDecidedEvent;
import com.company.skillplatform.approval.repository.ApprovalRequestRepository;
import com.company.skillplatform.approval.service.AdminApprovalService;
import com.company.skillplatform.common.exception.BusinessException;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminApprovalServiceImpl implements AdminApprovalService {

    private final ApprovalRequestRepository repository;
    private final ApplicationEventPublisher publisher;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminApprovalRequestItemResponse> assignedToMe(UUID adminId, AdminApprovalFilter filter, Pageable pageable) {
        Page<ApprovalRequest> page = resolveQuery(adminId, filter, pageable);
        return page.map(this::toItem);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminApprovalRequestDetailsResponse myAssignedRequest(UUID adminId, UUID requestId) {
        ApprovalRequest r = repository.findByIdAndAssignedToUserId(requestId, adminId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.APPROVAL_REQUEST_NOT_FOUND, "Approval request not found"));
        return toDetails(r);
    }

    @Override
    public void approve(UUID adminId, UUID requestId) {
        ApprovalRequest r = repository.findByIdAndAssignedToUserId(requestId, adminId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.APPROVAL_REQUEST_NOT_FOUND, "Approval request not found"));

        if (r.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "Only PENDING requests can be approved");
        }

        r.approve(adminId);
        publisher.publishEvent(new ApprovalDecidedEvent(r.getId(), r.getStatus()));

    }

    @Override
    public void reject(UUID adminId, UUID requestId, String note) {
        ApprovalRequest r = repository.findByIdAndAssignedToUserId(requestId, adminId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.APPROVAL_REQUEST_NOT_FOUND, "Approval request not found"));

        if (r.getStatus() != com.company.skillplatform.approval.enums.ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "Only PENDING requests can be rejected");
        }

        r.reject(adminId, note);
        publisher.publishEvent(new ApprovalDecidedEvent(r.getId(), r.getStatus()));

    }

    private Page<ApprovalRequest> resolveQuery(UUID adminId, AdminApprovalFilter filter, Pageable pageable) {
        if (filter == null || (filter.status() == null && filter.type() == null)) {
            return repository.findByAssignedToUserId(adminId, pageable);
        }
        if (filter.status() != null && filter.type() != null) {
            return repository.findByAssignedToUserIdAndStatusAndType(adminId, filter.status(), filter.type(), pageable);
        }
        if (filter.status() != null) {
            return repository.findByAssignedToUserIdAndStatus(adminId, filter.status(), pageable);
        }
        return repository.findByAssignedToUserIdAndType(adminId, filter.type(), pageable);
    }

    private AdminApprovalRequestItemResponse toItem(ApprovalRequest r) {
        return new AdminApprovalRequestItemResponse(
                r.getId(),
                r.getType(),
                r.getStatus(),
                r.getRequestedByUserId(),
                r.getDepartmentContext(),
                r.getCreatedAt()
        );
    }

    private AdminApprovalRequestDetailsResponse toDetails(ApprovalRequest r) {
        return new AdminApprovalRequestDetailsResponse(
                r.getId(),
                r.getType(),
                r.getStatus(),
                r.getRequestedByUserId(),
                r.getAssignedToUserId(),
                r.getDepartmentContext(),
                r.getPayload(),
                r.getRequestKey(),
                r.getCreatedAt(),
                r.getReviewedAt(),
                r.getReviewedByUserId(),
                r.getReviewNote()
        );
    }

}

