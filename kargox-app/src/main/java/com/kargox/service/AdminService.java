package com.kargox.service;

import com.kargox.dto.admin.DashboardStatsResponse;
import com.kargox.dto.admin.UserAdminResponse;
import com.kargox.dto.common.PageResponse;
import com.kargox.entity.Role;

public interface AdminService {

    PageResponse<UserAdminResponse> searchUsers(Role role, String search, int page, int size);
    UserAdminResponse setUserActive(Long userId, boolean active);
    DashboardStatsResponse getDashboardStats();
}
