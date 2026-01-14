package com.company.skillplatform.user.service;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.user.dto.EmployeeCardResponse;

import java.util.UUID;

public interface EmployeeDirectoryService {
    CursorPageResponse<EmployeeCardResponse> colleagues(UUID viewerUserId, Integer limit, String cursor, String q);
    CursorPageResponse<EmployeeCardResponse> directory(Integer limit, String cursor, String q);
}
