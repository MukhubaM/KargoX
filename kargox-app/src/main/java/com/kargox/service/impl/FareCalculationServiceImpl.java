package com.kargox.service.impl;

import com.kargox.dto.booking.FareEstimateResponse;
import com.kargox.dto.booking.GoodsItemDto;
import com.kargox.entity.GoodsCategory;
import com.kargox.entity.GoodsSizeTier;
import com.kargox.entity.VehicleType;
import com.kargox.service.FareCalculationService;
import com.kargox.util.GeoUtils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class FareCalculationServiceImpl implements FareCalculationService {

    @Value("${kargox.fare.base-fare}")
    private double baseFare;

    @Value("${kargox.fare.per-km-rate}")
    private double perKmRate;

    @Value("${kargox.fare.per-load-unit-rate}")
    private double perLoadUnitRate;

    @Value("${kargox.fare.per-extra-item-rate}")
    private double perExtraItemRate;

    @Value("${kargox.fare.currency}")
    private String currency;

    private static final Map<GoodsSizeTier, Integer> LOAD_UNITS = Map.of(
            GoodsSizeTier.SMALL, 1,
            GoodsSizeTier.MEDIUM, 4,
            GoodsSizeTier.LARGE, 10
    );

    private static final Map<GoodsSizeTier, VehicleType> MIN_VEHICLE_FOR_TIER = Map.of(
            GoodsSizeTier.SMALL, VehicleType.MOTORCYCLE,
            GoodsSizeTier.MEDIUM, VehicleType.HATCHBACK,
            GoodsSizeTier.LARGE, VehicleType.PICKUP_TRUCK
    );

    private static final List<VehicleType> RANK_ORDER = List.of(
            VehicleType.MOTORCYCLE, VehicleType.HATCHBACK, VehicleType.SEDAN,
            VehicleType.PICKUP_TRUCK, VehicleType.VAN, VehicleType.TRUCK
    );

    @Override
    public VehicleType recommendVehicleType(List<GoodsItemDto> goodsItems) {

        boolean forcesTruck = goodsItems.stream().anyMatch(i ->
                i.getCategory() == GoodsCategory.LARGE_CARGO || i.getCategory() == GoodsCategory.BUILDING_MATERIALS);
        if (forcesTruck) {

            return VehicleType.TRUCK;
        }

        int totalLoadUnits = goodsItems.stream()
                .mapToInt(i -> LOAD_UNITS.get(i.getSizeTier()) * qty(i))
                .sum();
        int totalQuantity = goodsItems.stream().mapToInt(this::qty).sum();

        VehicleType byLoad = vehicleForLoadUnits(totalLoadUnits);

        VehicleType byLargestItem = goodsItems.stream()
                .map(i -> MIN_VEHICLE_FOR_TIER.get(i.getSizeTier()))
                .max(Comparator.comparingInt(RANK_ORDER::indexOf))
                .orElse(VehicleType.MOTORCYCLE);

        VehicleType byQuantity = totalQuantity > 20 ? VehicleType.VAN
                : totalQuantity > 10 ? VehicleType.PICKUP_TRUCK
                : VehicleType.MOTORCYCLE;

        return maxRank(maxRank(byLoad, byLargestItem), byQuantity);
    }

    @Override
    public FareEstimateResponse estimate(double pickupLat, double pickupLng, double destLat, double destLng, List<GoodsItemDto> goodsItems) {

        VehicleType vehicleType = recommendVehicleType(goodsItems);
        double distanceKm = GeoUtils.distanceKm(pickupLat, pickupLng, destLat, destLng);
        int etaMinutes = GeoUtils.estimatedMinutes(distanceKm);

        int totalLoadUnits = goodsItems.stream()
                .mapToInt(i -> LOAD_UNITS.get(i.getSizeTier()) * qty(i))
                .sum();
        int totalQuantity = goodsItems.stream().mapToInt(this::qty).sum();
        int extraItems = Math.max(0, totalQuantity - 1);

        double vehicleMultiplier = vehicleMultiplier(vehicleType);

        double distanceCost = distanceKm * perKmRate * vehicleMultiplier;
        double loadCost = totalLoadUnits * perLoadUnitRate;
        double extraItemsCost = extraItems * perExtraItemRate;
        double fare = baseFare + distanceCost + loadCost + extraItemsCost;

        fare = Math.round(fare * 100.0) / 100.0;
        distanceKm = Math.round(distanceKm * 100.0) / 100.0;

        String breakdown = String.format(
                "Base fare: %.2f, Distance (%.2f km x %.2f x %.1f vehicle multiplier): %.2f, "
                        + "Load (%d units x %.2f): %.2f, Handling (%d extra item%s x %.2f): %.2f",
                baseFare, distanceKm, perKmRate, vehicleMultiplier, distanceCost,
                totalLoadUnits, perLoadUnitRate, loadCost,
                extraItems, extraItems == 1 ? "" : "s", perExtraItemRate, extraItemsCost);

        return new FareEstimateResponse(vehicleType, distanceKm, etaMinutes, fare, currency, breakdown);
    }

    private VehicleType vehicleForLoadUnits(int totalLoadUnits) {

        if (totalLoadUnits > 30) return VehicleType.TRUCK;
        if (totalLoadUnits > 14) return VehicleType.VAN;
        if (totalLoadUnits > 6) return VehicleType.PICKUP_TRUCK;
        if (totalLoadUnits > 3) return VehicleType.SEDAN;
        if (totalLoadUnits > 1) return VehicleType.HATCHBACK;

        return VehicleType.MOTORCYCLE;
    }

    private VehicleType maxRank(VehicleType a, VehicleType b) {

        return RANK_ORDER.indexOf(a) >= RANK_ORDER.indexOf(b) ? a : b;
    }

    private double vehicleMultiplier(VehicleType type) {

        return switch (type) {
            case MOTORCYCLE -> 0.8;
            case HATCHBACK -> 1.0;
            case SEDAN -> 1.1;
            case PICKUP_TRUCK -> 1.4;
            case VAN -> 1.8;
            case TRUCK -> 2.5;
        };
    }

    private int qty(GoodsItemDto item) {

        return item.getQuantity() == null ? 1 : item.getQuantity();
    }
}
