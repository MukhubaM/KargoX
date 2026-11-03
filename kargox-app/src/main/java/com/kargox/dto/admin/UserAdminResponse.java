package com.kargox.dto.admin;

import com.kargox.entity.Role;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserAdminResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private boolean active;
    private boolean emailVerified;
    private Double averageRating;
    private Instant createdAt;
    private Instant lastLoginAt;
}
