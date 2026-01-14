package com.company.skillplatform.admin.controller;

import com.company.skillplatform.admin.dto.ChangeUserRoleRequest;
import com.company.skillplatform.admin.service.AdminUserRoleService;
import com.company.skillplatform.auth.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserRoleController {

    private final AdminUserRoleService adminUserRoleService;

    @PostMapping("/change-role")
    public void changeRole(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        adminUserRoleService.changeUserRole(principal.getId(), request);
    }
}
