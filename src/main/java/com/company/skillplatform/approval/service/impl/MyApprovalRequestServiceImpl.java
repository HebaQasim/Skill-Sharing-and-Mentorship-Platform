package com.company.skillplatform.approval.service.impl;

import com.company.skillplatform.approval.dto.MyApprovalFilter;
import com.company.skillplatform.approval.dto.MyApprovalRequestDetailsResponse;
import com.company.skillplatform.approval.dto.MyApprovalRequestItemResponse;
import com.company.skillplatform.approval.entity.ApprovalRequest;
import com.company.skillplatform.approval.enums.ApprovalStatus;
import com.company.skillplatform.approval.repository.ApprovalRequestRepository;
import com.company.skillplatform.approval.service.MyApprovalRequestService;
import com.company.skillplatform.common.exception.BusinessException;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ForbiddenException;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MyApprovalRequestServiceImpl implements MyApprovalRequestService {

    private final ApprovalRequestRepository repository;

    @Override
    public Page<MyApprovalRequestItemResponse> myRequests(UUID userId, MyApprovalFilter filter, Pageable pageable) {
        Page<ApprovalRequest> page = resolveQuery(userId, filter, pageable);
        return page.map(this::toResponse);
    }

    private Page<ApprovalRequest> resolveQuery(UUID userId, MyApprovalFilter filter, Pageable pageable) {
        if (filter == null || (filter.status() == null && filter.type() == null)) {
            return repository.findByRequestedByUserId(userId, pageable);
        }
        if (filter.status() != null && filter.type() != null) {
            return repository.findByRequestedByUserIdAndStatusAndType(userId, filter.status(), filter.type(), pageable);
        }
        if (filter.status() != null) {
            return repository.findByRequestedByUserIdAndStatus(userId, filter.status(), pageable);
        }
        return repository.findByRequestedByUserIdAndType(userId, filter.type(), pageable);
    }

    private MyApprovalRequestItemResponse toResponse(ApprovalRequest r) {
        return new MyApprovalRequestItemResponse(
                r.getId(),
                r.getType(),
                r.getStatus(),
                r.getDepartmentContext(),
                r.getCreatedAt(),
                r.getReviewedAt(),
                r.getReviewNote()
        );
    }

    @Override
    public MyApprovalRequestDetailsResponse myRequest(UUID userId, UUID requestId) {
        ApprovalRequest request = repository
                .findByIdAndRequestedByUserId(requestId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SESSION_NOT_FOUND,
                        "Approval request not found"
                ));

        return toDetailsResponse(request);
    }

    private MyApprovalRequestDetailsResponse toDetailsResponse(ApprovalRequest r) {
        return new MyApprovalRequestDetailsResponse(
                r.getId(),
                r.getType(),
                r.getStatus(),
                r.getDepartmentContext(),
                r.getPayload(),
                r.getRequestKey(),
                r.getCreatedAt(),
                r.getReviewedAt(),
                r.getReviewedByUserId(),
                r.getReviewNote()
        );
    }
    @Override
    public void cancel(UUID userId, UUID requestId) {
        ApprovalRequest req = repository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.APPROVAL_REQUEST_NOT_FOUND, "Request not found"));

        if (!req.getRequestedByUserId().equals(userId)) {
            throw new ForbiddenException("You cannot cancel someone else's request");
        }

        if (req.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT,"Only pending requests can be canceled");
        }

        req.cancel(userId);
        repository.save(req);
    }
}

