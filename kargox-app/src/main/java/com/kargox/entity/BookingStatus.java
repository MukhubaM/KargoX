package com.kargox.entity;

public enum BookingStatus {

    REQUESTED,
    SEARCHING_DRIVER,
    DRIVER_ASSIGNED,
    DRIVER_EN_ROUTE,
    DRIVER_ARRIVED,
    GOODS_COLLECTED,
    IN_TRANSIT,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    NO_DRIVER_FOUND
}
