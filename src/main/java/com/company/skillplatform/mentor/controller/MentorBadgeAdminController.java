package com.company.skillplatform.mentor.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.mentor.dto.DecideMentorRequest;
import com.company.skillplatform.mentor.service.MentorBadgeAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@PreAuthorize("hasAnyRole('ADMIN','DEPARTMENT_ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/mentor-requests")
public class MentorBadgeAdminController {

    private final MentorBadgeAdminService mentorBadgeAdminService;

    @PostMapping("/{id}/approve")
    public void approve(@AuthenticationPrincipal UserPrincipal me,
                        @PathVariable UUID id,
                        @Valid @RequestBody DecideMentorRequest req) {
        mentorBadgeAdminService.approve(me.getId(), id, req);
    }

    @PostMapping("/{id}/reject")
    public void reject(@AuthenticationPrincipal UserPrincipal me,
                       @PathVariable UUID id,
                       @Valid @RequestBody DecideMentorRequest req) {
        mentorBadgeAdminService.reject(me.getId(), id, req);
    }
}

