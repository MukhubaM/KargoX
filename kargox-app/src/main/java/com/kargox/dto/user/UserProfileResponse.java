package com.kargox.dto.user;

import com.kargox.entity.Role;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private String profilePhotoUrl;
    private boolean emailVerified;
    private Double averageRating;
    private Integer totalRatings;
    private Instant createdAt;
}
