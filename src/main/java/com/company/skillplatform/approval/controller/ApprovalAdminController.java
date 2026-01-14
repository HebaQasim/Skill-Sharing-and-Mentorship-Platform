package com.company.skillplatform.approval.controller;

import com.company.skillplatform.approval.dto.*;
import com.company.skillplatform.approval.enums.AdminApprovalSort;
import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;
import com.company.skillplatform.approval.service.AdminApprovalService;
import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.PagedResponse;
import com.company.skillplatform.common.web.PageRequests;
import com.company.skillplatform.common.web.Paging;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEPARTMENT_ADMIN') or hasRole('ADMIN')")
public class ApprovalAdminController {

    private final AdminApprovalService adminApprovalService;

    @GetMapping
    public ResponseEntity<PagedResponse<AdminApprovalRequestItemResponse>> assignedToMe(
            @AuthenticationPrincipal UserPrincipal admin,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) ApprovalRequestType type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "NEWEST") AdminApprovalSort sort
    ) {
        Pageable pageable = PageRequests.of(page, size, sort.toSort());
        var filter = new AdminApprovalFilter(status, type);

        var result = adminApprovalService.assignedToMe(admin.getId(), filter, pageable);
        return ResponseEntity.ok(Paging.toPagedResponse(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminApprovalRequestDetailsResponse> details(
            @AuthenticationPrincipal UserPrincipal admin,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(adminApprovalService.myAssignedRequest(admin.getId(), id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(
            @AuthenticationPrincipal UserPrincipal admin,
            @PathVariable UUID id
    ) {
        adminApprovalService.approve(admin.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal UserPrincipal admin,
            @PathVariable UUID id,
            @Valid @RequestBody RejectApprovalRequest request
    ) {
        adminApprovalService.reject(admin.getId(), id, request.note());
        return ResponseEntity.noContent().build();
    }
}
