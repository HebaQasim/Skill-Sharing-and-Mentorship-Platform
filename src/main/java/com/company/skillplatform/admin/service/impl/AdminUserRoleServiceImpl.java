package com.company.skillplatform.admin.service.impl;

import com.company.skillplatform.auth.entity.Role;
import com.company.skillplatform.auth.enums.RoleName;
import com.company.skillplatform.auth.repository.RoleRepository;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.admin.dto.ChangeUserRoleRequest;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.admin.service.AdminUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserRoleServiceImpl implements AdminUserRoleService {


    public static final String EMPLOYEE_DIRECTORY_STAMP = "employeeDirectory";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CacheStampService cacheStampService;
    private final NotificationRepository notificationRepository;


    @Override
    public void changeUserRole(UUID actorUserId, ChangeUserRoleRequest request) {


        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "Actor not found"));

        boolean isGlobalAdmin = actor.getRoles().stream()
                .anyMatch(r -> ("ROLE_" + RoleName.ADMIN.name()).equalsIgnoreCase(r.getName().name())
                        || RoleName.ADMIN.name().equalsIgnoreCase(r.getName().name()));

        if (!isGlobalAdmin) {
            throw new ForbiddenException("Only Global Admin can change user roles");
        }


        User target = userRepository.findByIdForUpdate(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));


        boolean targetIsGlobalAdmin = target.getRoles().stream()
                .anyMatch(r -> r.getName().name().equalsIgnoreCase(RoleName.ADMIN.name()));
        if (targetIsGlobalAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Cannot change role of Global Admin");
        }


        Role employeeRole = mustRole(RoleName.EMPLOYEE);
        Role deptAdminRole = mustRole(RoleName.DEPARTMENT_ADMIN);


        Set<Role> newRoles = new HashSet<>();
        newRoles.add(employeeRole);

        if (request.targetRole() == ChangeUserRoleRequest.TargetRole.DEPARTMENT_ADMIN) {
            newRoles.add(deptAdminRole);
        }

        target.getRoles().clear();
        target.getRoles().addAll(newRoles);


        cacheStampService.bump(EMPLOYEE_DIRECTORY_STAMP);
        String roleLabel = (request.targetRole() == ChangeUserRoleRequest.TargetRole.DEPARTMENT_ADMIN)
                ? "Department Admin"
                : "Employee";

        notificationRepository.save(Notification.builder()
                .recipientUserId(target.getId())
                .type(NotificationType.USER_ROLE_UPDATED)
                .title("Role updated")
                .message("Your role has been updated to: " + roleLabel)
                .link("/profile")
                .build());


    }

    private Role mustRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "Role not found: " + roleName));
    }
}
