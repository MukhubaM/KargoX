package com.kargox.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class DashboardStatsResponse {

    private long totalUsers;
    private long totalCustomers;
    private long totalDrivers;
    private long activeDrivers;
    private long totalBookings;
    private long completedBookings;
    private long cancelledBookings;
    private long bookingsInProgress;
    private double totalRevenue;
    private Map<String, Long> bookingsByStatus;
}
