package com.kargox.service;

import com.kargox.dto.rating.RatingRequest;
import com.kargox.dto.rating.RatingResponse;

import java.util.List;

public interface RatingService {

    RatingResponse rate(Long bookingId, Long raterUserId, RatingRequest request);
    List<RatingResponse> getForBooking(Long bookingId);
}
