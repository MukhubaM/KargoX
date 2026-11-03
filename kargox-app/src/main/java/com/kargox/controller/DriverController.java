package com.kargox.controller;

import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.driver.*;
import com.kargox.security.SecurityUser;
import com.kargox.service.DriverMatchingService;
import com.kargox.service.DriverService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Driver onboarding, status, location, job acceptance")
public class DriverController {

    private final DriverService driverService;
    private final DriverMatchingService driverMatchingService;

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<DriverResponse>> onboard(@AuthenticationPrincipal
                                                               SecurityUser user,
                                                               @Valid @RequestBody
                                                               DriverRegisterRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Driver profile created - pending verification", driverService.registerAsDriver(user.getId(), request)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DriverResponse>> me(@AuthenticationPrincipal SecurityUser user) {

        return ResponseEntity.ok(ApiResponse.ok(driverService.getByUserId(user.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> getById(@PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.ok(driverService.getById(id)));
    }

    @PatchMapping("/me/status")
    public ResponseEntity<ApiResponse<DriverResponse>> updateStatus(@AuthenticationPrincipal
                                                                    SecurityUser user,
                                                                    @Valid @RequestBody
                                                                    DriverStatusUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(driverService.updateStatus(user.getId(), request.getStatus())));
    }

    @PostMapping("/me/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(@AuthenticationPrincipal
                                                            SecurityUser user,
                                                            @Valid @RequestBody
                                                            DriverLocationUpdateRequest request) {

        driverService.updateLocation(user.getId(), request.getLatitude(), request.getLongitude());

        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/bookings/{bookingId}/accept")
    public ResponseEntity<ApiResponse<Void>> acceptBooking(@AuthenticationPrincipal
                                                           SecurityUser user,
                                                           @PathVariable
                                                           Long bookingId) {

        DriverResponse driver = driverService.getByUserId(user.getId());

        driverMatchingService.acceptBooking(bookingId, driver.getId());

        return ResponseEntity.ok(ApiResponse.ok("Booking accepted", null));
    }
}
