package com.kargox.dto.booking;

import com.kargox.entity.*;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class BookingResponse {

    private Long id;
    private String bookingReference;
    private BookingStatus status;

    private Long customerId;
    private String customerName;

    private Long driverId;
    private String driverName;
    private String driverPhone;
    private Double driverRating;
    private Double driverLatitude;
    private Double driverLongitude;

    private Long vehicleId;
    private VehicleType vehicleType;
    private String vehicleRegistration;

    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    private VehicleType recommendedVehicleType;
    private List<GoodsItemDto> goodsItems;
    private String deliveryInstructions;

    private Double estimatedDistanceKm;
    private Integer estimatedDurationMinutes;
    private Double estimatedFare;
    private Double finalFare;

    private boolean scheduled;
    private Instant scheduledFor;

    private String pickupPin;
    private String deliveryOtp;

    private Instant createdAt;
    private Instant driverAssignedAt;
    private Instant driverArrivedAt;
    private Instant goodsCollectedAt;
    private Instant deliveredAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String cancellationReason;
}
