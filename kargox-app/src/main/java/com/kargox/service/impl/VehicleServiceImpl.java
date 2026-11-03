package com.kargox.service.impl;

import com.kargox.dto.vehicle.VehicleRequest;
import com.kargox.dto.vehicle.VehicleResponse;
import com.kargox.entity.Driver;
import com.kargox.entity.Vehicle;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.DriverRepository;
import com.kargox.repository.VehicleRepository;
import com.kargox.service.AuditLogService;
import com.kargox.service.VehicleService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public VehicleResponse register(Long driverId, VehicleRequest request) {

        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        Vehicle vehicle = new Vehicle();
        vehicle.setDriver(driver);
        applyRequest(vehicle, request);
        vehicleRepository.save(vehicle);

        auditLogService.log("VEHICLE_REGISTERED", "Vehicle", vehicle.getId(), "driverId=" + driverId);

        return toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getForDriver(Long driverId) {

        return vehicleRepository.findByDriverIdAndDeletedFalse(driverId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VehicleResponse update(Long vehicleId, Long driverId, VehicleRequest request) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("This vehicle does not belong to you");
        }
        applyRequest(vehicle, request);
        vehicleRepository.save(vehicle);

        return toResponse(vehicle);
    }

    @Override
    @Transactional
    public void deactivate(Long vehicleId, Long driverId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("This vehicle does not belong to you");
        }
        vehicle.setActive(false);
        vehicleRepository.save(vehicle);
    }

    private void applyRequest(Vehicle vehicle, VehicleRequest request) {

        vehicle.setType(request.getType());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setColor(request.getColor());
        vehicle.setMaxWeightKg(request.getMaxWeightKg());
        vehicle.setLengthCm(request.getLengthCm());
        vehicle.setWidthCm(request.getWidthCm());
        vehicle.setHeightCm(request.getHeightCm());
        vehicle.setPhotoUrl(request.getPhotoUrl());
        vehicle.setRoadworthyDocumentUrl(request.getRoadworthyDocumentUrl());
    }

    private VehicleResponse toResponse(Vehicle v) {

        VehicleResponse r = new VehicleResponse();
        r.setId(v.getId());
        r.setDriverId(v.getDriver().getId());
        r.setType(v.getType());
        r.setMake(v.getMake());
        r.setModel(v.getModel());
        r.setRegistrationNumber(v.getRegistrationNumber());
        r.setColor(v.getColor());
        r.setMaxWeightKg(v.getMaxWeightKg());
        r.setLengthCm(v.getLengthCm());
        r.setWidthCm(v.getWidthCm());
        r.setHeightCm(v.getHeightCm());
        r.setPhotoUrl(v.getPhotoUrl());
        r.setActive(v.isActive());
        r.setDocumentsVerified(v.isDocumentsVerified());

        return r;
    }
}
