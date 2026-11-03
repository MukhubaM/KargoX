package com.kargox.service;

import com.kargox.dto.auth.*;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String refreshToken);
    void changePassword(Long userId, ChangePasswordRequest request);
    void verifyEmail(Long userId, String code);
}
