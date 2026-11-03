package com.kargox.dto.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class CreateBookingRequest {

    @NotBlank
    private String pickupAddress;

    @NotNull
    private Double pickupLatitude;

    @NotNull
    private Double pickupLongitude;

    @NotBlank
    private String destinationAddress;

    @NotNull
    private Double destinationLatitude;

    @NotNull
    private Double destinationLongitude;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GoodsItemDto> goodsItems;

    private String deliveryInstructions;

    private boolean scheduled = false;
    private Instant scheduledFor;
}
