package com.company.skillplatform.approval.service;

import com.company.skillplatform.approval.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminApprovalService {

    Page<AdminApprovalRequestItemResponse> assignedToMe(UUID adminId, AdminApprovalFilter filter, Pageable pageable);

    AdminApprovalRequestDetailsResponse myAssignedRequest(UUID adminId, UUID requestId);

    void approve(UUID adminId, UUID requestId);

    void reject(UUID adminId, UUID requestId, String note);


}
