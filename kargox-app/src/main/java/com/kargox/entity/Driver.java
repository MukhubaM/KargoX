package com.kargox.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "drivers")
public class Driver extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.PENDING_VERIFICATION;

    private String licenseNumber;
    private String licenseDocumentUrl;
    private String idDocumentUrl;
    private boolean documentsVerified = false;

    private Double currentLatitude;
    private Double currentLongitude;
    private Instant lastLocationUpdate;

    private Double averageRating;
    private Integer totalRatings = 0;
    private Integer completedDeliveries = 0;

    private Double walletBalance = 0.0;
}
