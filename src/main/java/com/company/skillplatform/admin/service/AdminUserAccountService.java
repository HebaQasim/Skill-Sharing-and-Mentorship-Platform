package com.company.skillplatform.admin.service;

import com.company.skillplatform.admin.dto.SetUserEnabledRequest;

import java.util.UUID;

public interface AdminUserAccountService {
    void setEnabled(UUID actorUserId, UUID targetUserId, SetUserEnabledRequest request);
}
