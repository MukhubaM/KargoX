package com.kargox.service;

import com.kargox.dto.booking.FareEstimateResponse;
import com.kargox.dto.booking.GoodsItemDto;
import com.kargox.entity.VehicleType;

import java.util.List;

public interface FareCalculationService {

    //This picks the smallest vehicle type that will be able to carry the described goods
    VehicleType recommendVehicleType(List<GoodsItemDto> goodsItems);

    FareEstimateResponse estimate(double pickupLat, double pickupLng, double destLat, double destLng, List<GoodsItemDto> goodsItems);
}
