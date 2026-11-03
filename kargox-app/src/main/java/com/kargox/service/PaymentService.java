package com.kargox.service;

import com.kargox.dto.common.PaymentResponse;

public interface PaymentService {

    PaymentResponse refund(Long bookingId, String reason);

    PaymentResponse getForBooking(Long bookingId);
}
