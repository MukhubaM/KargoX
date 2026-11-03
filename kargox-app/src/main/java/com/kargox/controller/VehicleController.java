package com.kargox.controller;

import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.vehicle.VehicleRequest;
import com.kargox.dto.vehicle.VehicleResponse;
import com.kargox.security.SecurityUser;
import com.kargox.service.DriverService;
import com.kargox.service.VehicleService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Driver vehicle registration and management")
public class VehicleController {

    private final VehicleService vehicleService;
    private final DriverService driverService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> register(@AuthenticationPrincipal
                                                                 SecurityUser user,
                                                                 @Valid @RequestBody
                                                                 VehicleRequest request) {

        Long driverId = driverService.getByUserId(user.getId()).getId();

        return ResponseEntity.ok(ApiResponse.ok("Vehicle registered", vehicleService.register(driverId, request)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> myVehicles(@AuthenticationPrincipal SecurityUser user) {

        Long driverId = driverService.getByUserId(user.getId()).getId();

        return ResponseEntity.ok(ApiResponse.ok(vehicleService.getForDriver(driverId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> update(@AuthenticationPrincipal
                                                               SecurityUser user,
                                                               @PathVariable
                                                               Long id,
                                                               @Valid @RequestBody
                                                               VehicleRequest request) {

        Long driverId = driverService.getByUserId(user.getId()).getId();

        return ResponseEntity.ok(ApiResponse.ok(vehicleService.update(id, driverId, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@AuthenticationPrincipal
                                                        SecurityUser user,
                                                        @PathVariable
                                                        Long id) {

        Long driverId = driverService.getByUserId(user.getId()).getId();

        vehicleService.deactivate(id, driverId);

        return ResponseEntity.ok(ApiResponse.ok("Vehicle deactivated", null));
    }
}
