package com.kargox.controller;

import com.kargox.dto.auth.*;
import com.kargox.dto.common.ApiResponse;
import com.kargox.security.SecurityUser;
import com.kargox.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, tokens")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Account created", authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal
                                                            SecurityUser user,
                                                            @Valid @RequestBody
                                                            ChangePasswordRequest request) {

        authService.changePassword(user.getId(), request);

        return ResponseEntity.ok(ApiResponse.ok("Password changed", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@AuthenticationPrincipal
                                                         SecurityUser user,
                                                         @RequestParam
                                                         String code) {

        authService.verifyEmail(user.getId(), code);

        return ResponseEntity.ok(ApiResponse.ok("Email verified", null));
    }
}
