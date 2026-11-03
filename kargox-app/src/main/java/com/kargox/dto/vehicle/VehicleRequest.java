package com.kargox.dto.vehicle;

import com.kargox.entity.VehicleType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleRequest {

    @NotNull
    private VehicleType type;

    @NotBlank
    private String make;

    @NotBlank
    private String model;

    @NotBlank
    private String registrationNumber;

    private String color;

    @Positive
    private Double maxWeightKg;

    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;

    private String photoUrl;
    private String roadworthyDocumentUrl;
}
