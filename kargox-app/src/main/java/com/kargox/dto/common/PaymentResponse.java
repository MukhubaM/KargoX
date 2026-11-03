package com.kargox.dto.common;

import com.kargox.entity.PaymentMethod;
import com.kargox.entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private Double amount;
    private String currency;
    private PaymentMethod method;
    private PaymentStatus status;
    private String mockTransactionRef;
    private Instant paidAt;
    private Instant refundedAt;
    private String refundReason;
}
