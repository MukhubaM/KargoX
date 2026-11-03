package com.kargox.service.impl;

import com.kargox.entity.*;
import com.kargox.repository.DriverRepository;
import com.kargox.repository.VehicleRepository;
import com.kargox.exception.BadRequestException;
import com.kargox.exception.InvalidBookingStateException;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.BookingRepository;
import com.kargox.service.DriverMatchingService;
import com.kargox.service.NotificationService;
import com.kargox.util.GeoUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Driver Matching Engine
// Finds online drivers within the radius who own a vehicle suitable for the booking
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverMatchingServiceImpl implements DriverMatchingService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Value("${kargox.matching.search-radius-km}")
    private double searchRadiusKm;

    @Value("${kargox.matching.max-drivers-notified}")
    private int maxDriversNotified;

    // Vehicle capability ranking
    // A driver with a bigger vehicle can always carry a booking that only needs a smaller one
    private static final Map<VehicleType, Integer> CAPACITY_RANK = Map.of(
            VehicleType.MOTORCYCLE, 1,
            VehicleType.HATCHBACK, 2,
            VehicleType.SEDAN, 3,
            VehicleType.PICKUP_TRUCK, 4,
            VehicleType.VAN, 5,
            VehicleType.TRUCK, 6
    );

    @Override
    @Transactional
    public List<Driver> findAndNotifyNearbyDrivers(Booking booking) {

        List<Driver> candidates = driverRepository.findAvailableWithLocation(DriverStatus.ONLINE);

        int requiredRank = CAPACITY_RANK.get(booking.getRecommendedVehicleType());

        List<Driver> ranked = candidates.stream()
                .filter(d -> hasSuitableVehicle(d, requiredRank))
                .map(d -> Map.entry(d, GeoUtils.distanceKm(booking.getPickupLatitude(), booking.getPickupLongitude(), d.getCurrentLatitude(), d.getCurrentLongitude())))
                .filter(e -> e.getValue() <= searchRadiusKm)
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .limit(maxDriversNotified)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        booking.setStatus(BookingStatus.SEARCHING_DRIVER);
        bookingRepository.save(booking);

        for (Driver driver : ranked) {

            messagingTemplate.convertAndSend("/topic/drivers/" + driver.getId() + "/requests", jobOfferPayload(booking));
            notificationService.notify(driver.getUser().getId(), NotificationType.NEW_BOOKING_REQUEST, "New delivery request", "Pickup at " + booking.getPickupAddress() + " — estimated " + booking.getEstimatedFare() + " fare", booking.getId());
        }

        if (ranked.isEmpty()) {

            booking.setStatus(BookingStatus.NO_DRIVER_FOUND);
            bookingRepository.save(booking);
            log.warn("No suitable drivers found for booking {}", booking.getBookingReference());
        }

        return ranked;
    }

    @Override
    @Transactional
    public void acceptBooking(Long bookingId, Long driverId) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.SEARCHING_DRIVER) {
            throw new InvalidBookingStateException("This booking is no longer available for acceptance");
        }

        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (driver.getStatus() != DriverStatus.ONLINE) {
            throw new BadRequestException("Driver is not online");
        }

        List<Vehicle> vehicles = vehicleRepository.findByDriverIdAndActiveTrueAndDeletedFalse(driverId);
        Vehicle suitable = vehicles.stream()
                .filter(v -> CAPACITY_RANK.get(v.getType()) >= CAPACITY_RANK.get(booking.getRecommendedVehicleType()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("No suitable vehicle registered for this booking"));

        booking.setDriver(driver);
        booking.setVehicle(suitable);
        booking.setStatus(BookingStatus.DRIVER_ASSIGNED);
        booking.setDriverAssignedAt(Instant.now());
        bookingRepository.save(booking);

        driver.setStatus(DriverStatus.ON_TRIP);
        driverRepository.save(driver);

        messagingTemplate.convertAndSend("/topic/bookings/" + booking.getId(), "DRIVER_ASSIGNED");

        notificationService.notify(booking.getCustomer().getId(), NotificationType.DRIVER_ASSIGNED, "Driver assigned", driver.getUser().getFullName() + " is on the way to collect your goods", booking.getId());
    }

    private boolean hasSuitableVehicle(Driver driver, int requiredRank) {

        return vehicleRepository.findByDriverIdAndActiveTrueAndDeletedFalse(driver.getId()).stream().anyMatch(v -> CAPACITY_RANK.get(v.getType()) >= requiredRank);
    }

    private Map<String, Object> jobOfferPayload(Booking booking) {

        return Map.of(
                "bookingId", booking.getId(),
                "bookingReference", booking.getBookingReference(),
                "pickupAddress", booking.getPickupAddress(),
                "destinationAddress", booking.getDestinationAddress(),
                "estimatedFare", booking.getEstimatedFare(),
                "estimatedDistanceKm", booking.getEstimatedDistanceKm(),
                "recommendedVehicleType", booking.getRecommendedVehicleType()
        );
    }
}
