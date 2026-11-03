package com.kargox.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    // Pickup
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    // Destination
    private String destinationAddress;
    private Double destinationLatitude;
    private Double destinationLongitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType recommendedVehicleType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.REQUESTED;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsItem> goodsItems = new ArrayList<>();

    private String deliveryInstructions;

    private Double estimatedDistanceKm;
    private Integer estimatedDurationMinutes;

    private Double estimatedFare;
    private Double finalFare;

    private boolean scheduled = false;
    private Instant scheduledFor;

    // Lifecycle timestamps
    private Instant driverAssignedAt;
    private Instant driverArrivedAt;
    private Instant goodsCollectedAt;
    private Instant deliveredAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String cancellationReason;
    private String cancelledByRole; // CUSTOMER/DRIVER/ADMIN/SYSTEM

    // Proof of pickup
    private String pickupPin;
    private String pickupPhotoUrl;
    private String pickupSignatureUrl;

    // Proof of delivery
    private String deliveryOtp;
    private String deliveryPhotoUrl;
    private String deliverySignatureUrl;
}
