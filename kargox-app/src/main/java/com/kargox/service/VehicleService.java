package com.kargox.service;

import com.kargox.dto.vehicle.VehicleRequest;
import com.kargox.dto.vehicle.VehicleResponse;

import java.util.List;

public interface VehicleService {

    VehicleResponse register(Long driverId, VehicleRequest request);
    List<VehicleResponse> getForDriver(Long driverId);
    VehicleResponse update(Long vehicleId, Long driverId, VehicleRequest request);
    void deactivate(Long vehicleId, Long driverId);
}
