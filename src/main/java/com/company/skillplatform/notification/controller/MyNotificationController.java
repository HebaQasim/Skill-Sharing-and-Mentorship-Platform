package com.company.skillplatform.notification.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.notification.dto.NotificationResponse;
import com.company.skillplatform.notification.dto.UnreadCountResponse;
import com.company.skillplatform.notification.service.MyNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications/me")
@RequiredArgsConstructor
public class MyNotificationController {

    private final MyNotificationService service;

    @GetMapping
    public ResponseEntity<CursorPageResponse<NotificationResponse>> myNotifications(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(service.myNotifications(user.getId(), limit, cursor));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id
    ) {
        service.markRead(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unread")
    public ResponseEntity<Void> markUnread(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID id
    ) {
        service.markUnread(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(service.unreadCount(user.getId()));
    }

}

