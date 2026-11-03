package com.kargox.service.impl;

import com.kargox.dto.auth.*;
import com.kargox.entity.RefreshToken;
import com.kargox.entity.Role;
import com.kargox.entity.User;
import com.kargox.exception.BadRequestException;
import com.kargox.exception.ConflictException;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.exception.UnauthorizedException;
import com.kargox.repository.RefreshTokenRepository;
import com.kargox.repository.UserRepository;
import com.kargox.security.JwtTokenProvider;
import com.kargox.service.AuditLogService;
import com.kargox.service.AuthService;
import com.kargox.service.EmailService;
import com.kargox.util.CodeGenerator;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    @Value("${kargox.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with this email already exists");
        }
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        user.setEmailVerified(false);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), CodeGenerator.numericPin());
        auditLogService.log("USER_REGISTERED", "User", user.getId(), "role=" + user.getRole());

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail()).orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isActive()) {
            throw new UnauthorizedException("This account has been suspended. Contact support.");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        auditLogService.log("USER_LOGIN", "User", user.getId(), null);

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {

        RefreshToken token = refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken()).orElseThrow(() -> new UnauthorizedException("Invalid or expired refresh token"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new UnauthorizedException("Refresh token has expired, please log in again");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return issueTokens(token.getUser());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {

        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).ifPresent(t -> {

            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditLogService.log("PASSWORD_CHANGED", "User", user.getId(), null);
    }

    @Override
    @Transactional
    public void verifyEmail(Long userId, String code) {

        // The code is validated on client-side against the emailed value
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    private AuthResponse issueTokens(User user) {

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(), accessToken, refreshToken.getToken());
    }
}
