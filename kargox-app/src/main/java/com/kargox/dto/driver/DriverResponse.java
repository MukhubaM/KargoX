package com.kargox.dto.driver;

import com.kargox.entity.DriverStatus;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private DriverStatus status;
    private String licenseNumber;
    private boolean documentsVerified;
    private Double currentLatitude;
    private Double currentLongitude;
    private Double averageRating;
    private Integer totalRatings;
    private Integer completedDeliveries;
    private Double walletBalance;
}
