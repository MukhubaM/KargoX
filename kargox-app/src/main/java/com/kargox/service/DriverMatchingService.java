package com.kargox.service;

import com.kargox.entity.Booking;
import com.kargox.entity.Driver;

import java.util.List;

public interface DriverMatchingService {

    // This finds nearby, online, suitably vehicles and drivers ranked by distance to the pickup point and also broadcasts the job offer to them over WebSocket
    List<Driver> findAndNotifyNearbyDrivers(Booking booking);

    // Called when the driver accepts the broadcasted job offer(Request)
    void acceptBooking(Long bookingId, Long driverId);
}
