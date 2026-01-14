package com.company.skillplatform.session.controller;

import com.company.skillplatform.session.dto.AttachRecordingRequest;
import com.company.skillplatform.session.service.SessionRecordingService;
import com.company.skillplatform.auth.security.UserPrincipal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
public class SessionRecordingController {

    private final SessionRecordingService recordingService;

    @PostMapping("/{sessionId}/recording")
    public void attachRecording(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId,
            @Valid @RequestBody AttachRecordingRequest request
    ) {
        recordingService.attachRecording(user.getId(), sessionId, request);
    }

    @PutMapping("/{sessionId}/recording")
    public void updateRecording(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId,
            @Valid @RequestBody AttachRecordingRequest request
    ) {
        recordingService.updateRecording(user.getId(), sessionId, request);
    }

    @DeleteMapping("/{sessionId}/recording")
    public void deleteRecording(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID sessionId
    ) {
        recordingService.deleteRecording(user.getId(), sessionId);
    }

}
