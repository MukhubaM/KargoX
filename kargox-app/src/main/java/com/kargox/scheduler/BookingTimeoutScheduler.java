package com.kargox.scheduler;

import com.kargox.entity.Booking;
import com.kargox.entity.BookingStatus;
import com.kargox.entity.NotificationType;
import com.kargox.repository.BookingRepository;
import com.kargox.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingTimeoutScheduler {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    @Value("${kargox.matching.driver-response-timeout-seconds}")
    private long timeoutSeconds;

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void expireStaleSearches() {

        List<Booking> stale = bookingRepository.findByStatusAndDeletedFalse(BookingStatus.SEARCHING_DRIVER);
        Instant cutoff = Instant.now().minus(timeoutSeconds, ChronoUnit.SECONDS);

        for (Booking booking : stale) {
            if (booking.getCreatedAt().isBefore(cutoff)) {
                booking.setStatus(BookingStatus.NO_DRIVER_FOUND);
                bookingRepository.save(booking);
                notificationService.notify(booking.getCustomer().getId(), NotificationType.GENERIC,
                        "No drivers available",
                    "We couldn't find a nearby driver for booking " + booking.getBookingReference()
                             + ". Please try again.",
                        booking.getId());

                log.info("Booking {} expired, no driver accepted in time", booking.getBookingReference());
            }
        }
    }
}
