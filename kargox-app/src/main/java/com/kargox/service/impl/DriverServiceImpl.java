package com.kargox.service.impl;

import com.kargox.dto.driver.DriverRegisterRequest;
import com.kargox.dto.driver.DriverResponse;
import com.kargox.entity.Driver;
import com.kargox.entity.DriverStatus;
import com.kargox.entity.Role;
import com.kargox.entity.User;
import com.kargox.exception.BadRequestException;
import com.kargox.exception.ConflictException;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.DriverRepository;
import com.kargox.repository.UserRepository;
import com.kargox.service.AuditLogService;
import com.kargox.service.DriverService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public DriverResponse registerAsDriver(Long userId, DriverRegisterRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.DRIVER) {
            throw new BadRequestException("Only accounts registered with role DRIVER can complete driver onboarding");
        }
        if (driverRepository.findByUserIdAndDeletedFalse(userId).isPresent()) {
            throw new ConflictException("Driver profile already exists for this user");
        }

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setLicenseNumber(request.getLicenseNumber());
        driver.setLicenseDocumentUrl(request.getLicenseDocumentUrl());
        driver.setIdDocumentUrl(request.getIdDocumentUrl());
        driver.setStatus(DriverStatus.PENDING_VERIFICATION);
        driverRepository.save(driver);

        auditLogService.log("DRIVER_ONBOARDED", "Driver", driver.getId(), "userId=" + userId);

        return toResponse(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getByUserId(Long userId) {

        return toResponse(driverRepository.findByUserIdAndDeletedFalse(userId).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getById(Long driverId) {

        return toResponse(driverRepository.findById(driverId).orElseThrow(() -> new ResourceNotFoundException("Driver not found")));
    }

    @Override
    @Transactional
    public DriverResponse updateStatus(Long userId, DriverStatus status) {

        Driver driver = driverRepository.findByUserIdAndDeletedFalse(userId).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));

        if (!driver.isDocumentsVerified() && status == DriverStatus.ONLINE) {
            throw new BadRequestException("Your documents must be verified by an administrator before going online");
        }
        if (status == DriverStatus.ONLINE && (driver.getUser().getProfilePhotoUrl() == null
                || driver.getUser().getProfilePhotoUrl().isBlank())) {
            throw new BadRequestException("Add a profile photo before going online, so customers can recognize you");
        }
        if (driver.getStatus() == DriverStatus.ON_TRIP && status == DriverStatus.OFFLINE) {
            throw new BadRequestException("Cannot go offline while a delivery is in progress");
        }

        driver.setStatus(status);
        driverRepository.save(driver);

        return toResponse(driver);
    }

    @Override
    @Transactional
    public void updateLocation(Long userId, double latitude, double longitude) {

        Driver driver = driverRepository.findByUserIdAndDeletedFalse(userId).orElseThrow(() -> new ResourceNotFoundException("Driver profile not found"));
        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver.setLastLocationUpdate(Instant.now());
        driverRepository.save(driver);
    }

    private DriverResponse toResponse(Driver d) {

        DriverResponse r = new DriverResponse();
        r.setId(d.getId());
        r.setUserId(d.getUser().getId());
        r.setFullName(d.getUser().getFullName());
        r.setEmail(d.getUser().getEmail());
        r.setPhoneNumber(d.getUser().getPhoneNumber());
        r.setProfilePhotoUrl(d.getUser().getProfilePhotoUrl());
        r.setStatus(d.getStatus());
        r.setLicenseNumber(d.getLicenseNumber());
        r.setDocumentsVerified(d.isDocumentsVerified());
        r.setCurrentLatitude(d.getCurrentLatitude());
        r.setCurrentLongitude(d.getCurrentLongitude());
        r.setAverageRating(d.getAverageRating());
        r.setTotalRatings(d.getTotalRatings());
        r.setCompletedDeliveries(d.getCompletedDeliveries());
        r.setWalletBalance(d.getWalletBalance());

        return r;
    }
}
