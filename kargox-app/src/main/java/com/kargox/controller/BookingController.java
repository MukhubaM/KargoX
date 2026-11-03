package com.kargox.controller;

import com.kargox.dto.booking.*;
import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.common.PageResponse;
import com.kargox.entity.BookingStatus;
import com.kargox.security.SecurityUser;
import com.kargox.service.BookingService;
import com.kargox.service.DriverService;
import com.kargox.service.FareCalculationService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking lifecycle: request, track, deliver, complete")
public class BookingController {

    private final BookingService bookingService;
    private final FareCalculationService fareCalculationService;
    private final DriverService driverService;

    @PostMapping("/estimate-fare")
    public ResponseEntity<ApiResponse<FareEstimateResponse>> estimateFare(@Valid @RequestBody FareEstimateRequest request) {

        FareEstimateResponse estimate = fareCalculationService.estimate(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDestinationLatitude(), request.getDestinationLongitude(),
                request.getGoodsItems());

        return ResponseEntity.ok(ApiResponse.ok(estimate));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(@AuthenticationPrincipal
                                                               SecurityUser user,
                                                               @Valid @RequestBody
                                                               CreateBookingRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Booking requested", bookingService.createBooking(user.getId(), request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@AuthenticationPrincipal
                                                                SecurityUser user,
                                                                @PathVariable
                                                                Long id) {

        return ResponseEntity.ok(ApiResponse.ok(bookingService.getById(id, user.getId(), user.getRole())));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<ApiResponse<BookingResponse>> getByReference(@PathVariable String reference) {

        return ResponseEntity.ok(ApiResponse.ok(bookingService.getByReference(reference)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> myBookings(@AuthenticationPrincipal
                                                                                 SecurityUser user,
                                                                                 @RequestParam(defaultValue = "0")
                                                                                 int page,
                                                                                 @RequestParam(defaultValue = "10")
                                                                                 int size) {

        PageResponse<BookingResponse> result = "DRIVER".equals(user.getRole())
                ? bookingService.getForDriver(driverService.getByUserId(user.getId()).getId(), page, size)
                : bookingService.getForCustomer(user.getId(), page, size);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BookingResponse>> updateStatus(@AuthenticationPrincipal
                                                                     SecurityUser user,
                                                                     @PathVariable
                                                                     Long id,
                                                                     @Valid @RequestBody
                                                                     BookingStatusUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(bookingService.updateStatus(id, user.getId(), user.getRole(), request)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(@AuthenticationPrincipal
                                                               SecurityUser user,
                                                               @PathVariable
                                                               Long id,
                                                               @Valid @RequestBody
                                                               CancelBookingRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(bookingService.cancel(id, user.getId(), user.getRole(), request)));
    }
}
