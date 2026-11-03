package com.kargox.controller;

import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.rating.RatingRequest;
import com.kargox.dto.rating.RatingResponse;
import com.kargox.security.SecurityUser;
import com.kargox.service.RatingService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings/{bookingId}/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings", description = "Customer <-> driver ratings and reviews")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RatingResponse>> rate(@AuthenticationPrincipal
                                                            SecurityUser user,
                                                            @PathVariable
                                                            Long bookingId,
                                                            @Valid @RequestBody
                                                            RatingRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Rating submitted", ratingService.rate(bookingId, user.getId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getForBooking(@PathVariable Long bookingId) {

        return ResponseEntity.ok(ApiResponse.ok(ratingService.getForBooking(bookingId)));
    }
}
