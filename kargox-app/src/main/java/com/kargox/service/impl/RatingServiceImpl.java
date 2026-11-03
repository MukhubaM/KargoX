package com.kargox.service.impl;

import com.kargox.dto.rating.RatingRequest;
import com.kargox.dto.rating.RatingResponse;
import com.kargox.entity.*;
import com.kargox.exception.BadRequestException;
import com.kargox.exception.ConflictException;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.BookingRepository;
import com.kargox.repository.DriverRepository;
import com.kargox.repository.RatingRepository;
import com.kargox.repository.UserRepository;
import com.kargox.service.RatingService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    @Override
    @Transactional
    public RatingResponse rate(Long bookingId, Long raterUserId, RatingRequest request) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Only completed bookings can be rated");
        }

        User rater = userRepository.findById(raterUserId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCustomer = booking.getCustomer().getId().equals(raterUserId);
        boolean isDriver = booking.getDriver() != null && booking.getDriver().getUser().getId().equals(raterUserId);

        if (!isCustomer && !isDriver) {
            throw new BadRequestException("You are not a participant in this booking");
        }

        boolean alreadyRated = ratingRepository.findByBookingId(bookingId).stream().anyMatch(r -> r.getRatedBy().getId().equals(raterUserId));

        if (alreadyRated) {
            throw new ConflictException("You have already rated this booking");
        }

        Rating rating = new Rating();
        rating.setBooking(booking);
        rating.setRatedBy(rater);
        rating.setRatedByRole(isCustomer ? Role.CUSTOMER : Role.DRIVER);
        rating.setScore(request.getScore());
        rating.setComment(request.getComment());
        ratingRepository.save(rating);

        // Customer rates the driver, then driver's rolling average gets updated after rating
        if (isCustomer) {
            Driver driver = booking.getDriver();
            recalcDriverRating(driver, request.getScore());
        } else {
            recalcCustomerRating(booking.getCustomer(), request.getScore());
        }

        return toResponse(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getForBooking(Long bookingId) {

        return ratingRepository.findByBookingId(bookingId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private void recalcDriverRating(Driver driver, int newScore) {

        int total = driver.getTotalRatings() == null ? 0 : driver.getTotalRatings();
        double avg = driver.getAverageRating() == null ? 0 : driver.getAverageRating();
        double newAvg = ((avg * total) + newScore) / (total + 1);
        driver.setAverageRating(Math.round(newAvg * 100.0) / 100.0);
        driver.setTotalRatings(total + 1);
        driverRepository.save(driver);
    }

    private void recalcCustomerRating(User customer, int newScore) {

        int total = customer.getTotalRatings() == null ? 0 : customer.getTotalRatings();
        double avg = customer.getAverageRating() == null ? 0 : customer.getAverageRating();
        double newAvg = ((avg * total) + newScore) / (total + 1);
        customer.setAverageRating(Math.round(newAvg * 100.0) / 100.0);
        customer.setTotalRatings(total + 1);
        userRepository.save(customer);
    }

    private RatingResponse toResponse(Rating r) {

        RatingResponse dto = new RatingResponse();
        dto.setId(r.getId());
        dto.setBookingId(r.getBooking().getId());
        dto.setRatedByName(r.getRatedBy().getFullName());
        dto.setRatedByRole(r.getRatedByRole().name());
        dto.setScore(r.getScore());
        dto.setComment(r.getComment());
        dto.setCreatedAt(r.getCreatedAt());

        return dto;
    }
}
