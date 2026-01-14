package com.company.skillplatform.approval.controller;

import com.company.skillplatform.approval.dto.MyApprovalFilter;
import com.company.skillplatform.approval.dto.MyApprovalRequestDetailsResponse;
import com.company.skillplatform.approval.dto.MyApprovalRequestItemResponse;
import com.company.skillplatform.approval.enums.ApprovalRequestType;
import com.company.skillplatform.approval.enums.ApprovalStatus;
import com.company.skillplatform.approval.enums.MyApprovalSort;
import com.company.skillplatform.approval.service.MyApprovalRequestService;
import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.PagedResponse;
import com.company.skillplatform.common.web.PageRequests;
import com.company.skillplatform.common.web.Paging;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@PreAuthorize("hasAnyRole('EMPLOYEE')")
@RestController
@RequestMapping("/api/users/me/approval-requests")
@RequiredArgsConstructor
public class MyApprovalRequestController {

    private final MyApprovalRequestService service;

    @GetMapping
    public ResponseEntity<PagedResponse<MyApprovalRequestItemResponse>> myRequests(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) ApprovalRequestType type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "NEWEST") MyApprovalSort sort
    ) {
        Pageable pageable = PageRequests.of(page, size, sort.toSort());
        var filter = new MyApprovalFilter(status, type);

        var result =service.myRequests(user.getId(), filter, pageable);
        return ResponseEntity.ok(Paging.toPagedResponse(result));
    }
    @GetMapping("/{id}")
    public ResponseEntity<MyApprovalRequestDetailsResponse> myRequest(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                service.myRequest(user.getId(), id)
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id
    ) {
        service.cancel(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}

