package com.kargox.service;

import com.kargox.dto.user.UpdateProfileRequest;
import com.kargox.dto.user.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
}
