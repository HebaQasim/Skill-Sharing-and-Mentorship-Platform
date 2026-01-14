package com.company.skillplatform.user.service;

import java.util.UUID;

public interface UserPasswordService {
    void changePassword(UUID userId, String currentPassword, String newPassword);
}

