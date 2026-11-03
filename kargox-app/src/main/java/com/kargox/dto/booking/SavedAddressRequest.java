package com.kargox.dto.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedAddressRequest {

    @NotBlank
    private String label;

    @NotBlank
    private String formattedAddress;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
