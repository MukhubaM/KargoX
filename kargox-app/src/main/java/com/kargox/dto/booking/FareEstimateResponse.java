package com.kargox.dto.booking;

import com.kargox.entity.VehicleType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FareEstimateResponse {

    private VehicleType recommendedVehicleType;
    private double distanceKm;
    private int estimatedDurationMinutes;
    private double estimatedFare;
    private String currency;
    private String breakdown;
}
