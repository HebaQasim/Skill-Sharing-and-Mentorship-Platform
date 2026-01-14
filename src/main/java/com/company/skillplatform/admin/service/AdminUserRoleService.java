package com.company.skillplatform.admin.service;

import com.company.skillplatform.admin.dto.ChangeUserRoleRequest;

import java.util.UUID;

public interface AdminUserRoleService {
    void changeUserRole(UUID actorUserId, ChangeUserRoleRequest request);
}
