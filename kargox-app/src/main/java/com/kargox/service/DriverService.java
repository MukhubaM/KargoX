package com.kargox.service;

import com.kargox.dto.driver.DriverRegisterRequest;
import com.kargox.dto.driver.DriverResponse;
import com.kargox.entity.DriverStatus;

public interface DriverService {

    DriverResponse registerAsDriver(Long userId, DriverRegisterRequest request);
    DriverResponse getByUserId(Long userId);
    DriverResponse getById(Long driverId);
    DriverResponse updateStatus(Long userId, DriverStatus status);
    void updateLocation(Long userId, double latitude, double longitude);
}
