package com.kargox.service.impl;

import com.kargox.dto.user.UpdateProfileRequest;
import com.kargox.dto.user.UserProfileResponse;
import com.kargox.entity.User;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.UserRepository;
import com.kargox.service.AuditLogService;
import com.kargox.service.UserProfileService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    public UserProfileResponse getProfile(Long userId) {

        return toResponse(findUser(userId));
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {

        User user = findUser(userId);
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        userRepository.save(user);
        auditLogService.log("PROFILE_UPDATED", "User", user.getId(), null);

        return toResponse(user);
    }

    private User findUser(Long userId) {

        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserProfileResponse toResponse(User u) {

        UserProfileResponse r = new UserProfileResponse();
        r.setId(u.getId());
        r.setFullName(u.getFullName());
        r.setEmail(u.getEmail());
        r.setPhoneNumber(u.getPhoneNumber());
        r.setRole(u.getRole());
        r.setProfilePhotoUrl(u.getProfilePhotoUrl());
        r.setEmailVerified(u.isEmailVerified());
        r.setAverageRating(u.getAverageRating());
        r.setTotalRatings(u.getTotalRatings());
        r.setCreatedAt(u.getCreatedAt());

        return r;
    }
}
