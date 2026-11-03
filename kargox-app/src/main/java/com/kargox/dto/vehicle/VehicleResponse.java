package com.kargox.dto.vehicle;

import com.kargox.entity.VehicleType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleResponse {

    private Long id;
    private Long driverId;
    private VehicleType type;
    private String make;
    private String model;
    private String registrationNumber;
    private String color;
    private Double maxWeightKg;
    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;
    private String photoUrl;
    private boolean active;
    private boolean documentsVerified;
}
