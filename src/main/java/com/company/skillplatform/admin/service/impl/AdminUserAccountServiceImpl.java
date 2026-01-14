package com.company.skillplatform.admin.service.impl;

import com.company.skillplatform.auth.service.AuthService;
import com.company.skillplatform.common.cache.CacheStampService;
import com.company.skillplatform.common.exception.*;
import com.company.skillplatform.notification.entity.Notification;
import com.company.skillplatform.notification.enums.NotificationType;
import com.company.skillplatform.notification.repository.NotificationRepository;
import com.company.skillplatform.admin.dto.SetUserEnabledRequest;
import com.company.skillplatform.user.entity.User;
import com.company.skillplatform.user.repository.UserRepository;
import com.company.skillplatform.admin.service.AdminUserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserAccountServiceImpl implements AdminUserAccountService {

    public static final String EMPLOYEES_STAMP_NAME = "employees";

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final CacheStampService cacheStampService;
    private final AuthService authService;


    @Override
    public void setEnabled(UUID actorUserId, UUID targetUserId, SetUserEnabledRequest request) {

        if (actorUserId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_DISABLE_SELF, "You can't change your own enabled state");
        }

        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "Actor not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));


        boolean isGlobalAdmin = actor.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
        boolean isDeptAdmin = actor.getRoles().stream().anyMatch(r -> r.getName().equals("DEPARTMENT_ADMIN"));

        if (!isGlobalAdmin && !isDeptAdmin) {
            throw new ForbiddenException("Not allowed");
        }

        if (isDeptAdmin && !actor.getDepartment().equals(target.getDepartment())) {
            throw new BusinessException(ErrorCode.DEPARTMENT_SCOPE_VIOLATION, "You can manage users only in your department");
        }

        boolean newEnabled = request.enabled();
        if (target.isEnabled() == newEnabled) return;


        target.setEnabled(newEnabled);

        userRepository.save(target);
        if (!newEnabled) {
            authService.logoutAll(target.getId());
        }


        cacheStampService.bump(EMPLOYEES_STAMP_NAME);
    }
}

