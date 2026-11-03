package com.kargox.dto.auth;

import com.kargox.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private Long userId;
    private String fullName;
    private String email;
    private Role role;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public AuthResponse(Long userId, String fullName, String email, Role role, String accessToken, String refreshToken) {

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
    }
}
