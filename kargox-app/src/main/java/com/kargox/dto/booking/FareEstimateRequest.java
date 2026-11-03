package com.kargox.dto.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FareEstimateRequest {

    @NotNull
    private Double pickupLatitude;

    @NotNull
    private Double pickupLongitude;

    @NotNull
    private Double destinationLatitude;

    @NotNull
    private Double destinationLongitude;

    @NotEmpty
    @Valid
    private List<GoodsItemDto> goodsItems;
}
