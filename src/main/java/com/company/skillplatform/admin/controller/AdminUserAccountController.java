package com.company.skillplatform.admin.controller;

import com.company.skillplatform.admin.dto.SetUserEnabledRequest;
import com.company.skillplatform.admin.service.AdminUserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserAccountController {

    private final AdminUserAccountService adminUserAccountService;

    @PatchMapping("/{id}/enabled")
    public void setEnabled(
            @AuthenticationPrincipal com.company.skillplatform.auth.security.UserPrincipal me,
            @PathVariable("id") UUID targetUserId,
            @Valid @RequestBody SetUserEnabledRequest request
    ) {
        adminUserAccountService.setEnabled(me.getId(), targetUserId, request);
    }
}
