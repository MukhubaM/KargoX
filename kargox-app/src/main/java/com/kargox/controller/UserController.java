package com.kargox.controller;

import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.user.UpdateProfileRequest;
import com.kargox.dto.user.UserProfileResponse;
import com.kargox.security.SecurityUser;
import com.kargox.service.UserProfileService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Self-service profile: view and update your own account")
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me(@AuthenticationPrincipal SecurityUser user) {

        return ResponseEntity.ok(ApiResponse.ok(userProfileService.getProfile(user.getId())));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMe(@AuthenticationPrincipal SecurityUser user,
                                                                     @Valid @RequestBody UpdateProfileRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Profile updated", userProfileService.updateProfile(user.getId(), request)));
    }
}
