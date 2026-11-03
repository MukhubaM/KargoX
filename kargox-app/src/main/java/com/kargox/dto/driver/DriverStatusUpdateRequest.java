package com.kargox.dto.driver;

import com.kargox.entity.DriverStatus;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverStatusUpdateRequest {

    @NotNull
    private DriverStatus status;
}
