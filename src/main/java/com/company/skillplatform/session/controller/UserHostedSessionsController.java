package com.company.skillplatform.session.controller;

import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.session.dto.HostedSessionCardResponse;
import com.company.skillplatform.session.enums.SessionStatus;
import com.company.skillplatform.session.service.impl.UserHostedSessionsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserHostedSessionsController {

    private final UserHostedSessionsServiceImpl hostedSessionsService;

    @GetMapping("/{userId}/sessions")
    public CursorPageResponse<HostedSessionCardResponse> getUserHostedSessions(
            @PathVariable UUID userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Set<SessionStatus> statuses
    ) {
        return hostedSessionsService.getHostedSessions(userId, cursor, size, statuses);
    }

}
