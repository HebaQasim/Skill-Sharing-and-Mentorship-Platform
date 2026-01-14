package com.company.skillplatform.approval.service;

import java.util.UUID;

public interface ApprovalRoutingService {
    UUID resolveApproverForDepartment(String department);
}

