package com.company.skillplatform.user.controller;

import com.company.skillplatform.auth.security.UserPrincipal;
import com.company.skillplatform.common.dto.CursorPageResponse;
import com.company.skillplatform.post.dto.PostCardResponse;
import com.company.skillplatform.post.service.PostPublishedService;
import com.company.skillplatform.user.dto.ChangePasswordRequest;
import com.company.skillplatform.user.dto.MyProfileResponse;
import com.company.skillplatform.user.dto.MyProfileWithPostsResponse;
import com.company.skillplatform.user.dto.UpdateMyProfileRequest;
import com.company.skillplatform.user.service.MyProfileService;
import com.company.skillplatform.user.service.UserPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class MyProfileController {

    private final MyProfileService myProfileService;
    private final UserPasswordService passwordService;


    @GetMapping
    public ResponseEntity<MyProfileWithPostsResponse> me(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(
                myProfileService.meWithPosts(user.getId(), limit, cursor)
        );
    }


    @PatchMapping("/profile")
    public ResponseEntity<MyProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        return ResponseEntity.ok(myProfileService.update(user.getId(), request));
    }

    @PostMapping("/change-password")
    public void changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        passwordService.changePassword(
                principal.getId(),
                request.currentPassword(),
                request.newPassword()
        );
    }

    @GetMapping("/users/me/posts/deleted")
    public CursorPageResponse<PostCardResponse> myDeletedPosts(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        return myProfileService.myDeletedPosts(user.getId(), limit, cursor);
    }



}

