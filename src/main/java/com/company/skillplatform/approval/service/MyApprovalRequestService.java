package com.company.skillplatform.approval.service;

import com.company.skillplatform.approval.dto.MyApprovalFilter;
import com.company.skillplatform.approval.dto.MyApprovalRequestDetailsResponse;
import com.company.skillplatform.approval.dto.MyApprovalRequestItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MyApprovalRequestService {
    Page<MyApprovalRequestItemResponse> myRequests(UUID userId, MyApprovalFilter filter, Pageable pageable);
    void cancel(UUID userId, UUID requestId);
    MyApprovalRequestDetailsResponse myRequest(UUID userId, UUID requestId);

}

