package com.kargox.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    private String make;
    private String model;
    private String registrationNumber;
    private String color;

    private Double maxWeightKg;
    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;

    private String photoUrl;

    @Column(nullable = false)
    private boolean active = true;

    private String roadworthyDocumentUrl;
    private boolean documentsVerified = false;
}
