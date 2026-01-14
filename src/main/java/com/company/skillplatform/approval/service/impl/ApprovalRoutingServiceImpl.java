package com.company.skillplatform.approval.service.impl;

import com.company.skillplatform.admin.repository.DepartmentAdminRepository;
import com.company.skillplatform.approval.service.ApprovalRoutingService;
import com.company.skillplatform.auth.enums.RoleName;
import com.company.skillplatform.common.exception.ErrorCode;
import com.company.skillplatform.common.exception.ResourceNotFoundException;
import com.company.skillplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApprovalRoutingServiceImpl implements ApprovalRoutingService {

    private final DepartmentAdminRepository departmentAdminRepository;
    private final UserRepository userRepository;

    @Override
    public UUID resolveApproverForDepartment(String department) {

        // 1) Department admin
        var depAdmin = departmentAdminRepository.findByDepartmentIgnoreCase(department);
        if (depAdmin.isPresent()) {
            return depAdmin.get().getAdmin().getId();
        }

        // 2) Fallback: Global ADMIN
        return userRepository.findFirstByRole(RoleName.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "No global ADMIN found for fallback routing"
                ))
                .getId();
    }
}
