package com.kargox.dto.driver;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverLocationUpdateRequest {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
