package com.company.skillplatform.approval.service;

import com.company.skillplatform.approval.dto.MyApprovalFilter;
import com.company.skillplatform.approval.dto.MyApprovalRequestDetailsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ApprovalRequestService {
    UUID createOrUpdateProfileChangeRequest(UUID requesterId, String departmentContext, String payloadJson);


    UUID createOrUpdateSkillCreateRequest(UUID requesterId, String departmentContext, String payloadJson);
}





