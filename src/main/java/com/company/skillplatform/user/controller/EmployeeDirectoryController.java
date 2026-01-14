package com.company.skillplatform.user.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.user.dto.EmployeeCardResponse;
import com.company.skillplatform.user.dto.MyProfileWithPostsResponse;
import com.company.skillplatform.user.service.EmployeeDirectoryService;
import com.company.skillplatform.user.service.MyProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class EmployeeDirectoryController {

    private final EmployeeDirectoryService service;
    private final MyProfileService myProfileService;

    @GetMapping("/colleagues")
    public ResponseEntity<CursorPageResponse<EmployeeCardResponse>> colleagues(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, name = "q") String query
    ) {
        return ResponseEntity.ok(service.colleagues(user.getId(), limit, cursor, query));
    }

    @GetMapping("/directory")
    public ResponseEntity<CursorPageResponse<EmployeeCardResponse>> directory(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, name = "q") String query
    ) {
        return ResponseEntity.ok(service.directory(limit, cursor, query));
    }


    @GetMapping("/{profileUserId}")
    public ResponseEntity<MyProfileWithPostsResponse> userProfile(
            @AuthenticationPrincipal UserPrincipal viewer,
            @PathVariable UUID profileUserId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(
                myProfileService.profileWithPosts(viewer.getId(), profileUserId, limit, cursor)
        );
    }
}
