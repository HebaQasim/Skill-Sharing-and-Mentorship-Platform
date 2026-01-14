package com.company.skillplatform.admin.audit.controller;

import com.company.skillplatform.admin.audit.dto.AdminAuditLogResponse;
import com.company.skillplatform.admin.audit.service.AdminAuditQueryService;
import com.company.skillplatform.common.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {

    private final AdminAuditQueryService service;

    @GetMapping
    public ResponseEntity<CursorPageResponse<AdminAuditLogResponse>> list(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(service.list(limit, cursor));
    }
}

