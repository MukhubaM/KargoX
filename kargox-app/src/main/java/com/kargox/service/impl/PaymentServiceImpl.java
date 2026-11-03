package com.kargox.service.impl;

import com.kargox.dto.common.PaymentResponse;
import com.kargox.entity.Driver;
import com.kargox.entity.NotificationType;
import com.kargox.entity.Payment;
import com.kargox.entity.PaymentStatus;
import com.kargox.exception.BadRequestException;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.DriverRepository;
import com.kargox.repository.PaymentRepository;
import com.kargox.service.AuditLogService;
import com.kargox.service.NotificationService;
import com.kargox.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final DriverRepository driverRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    // Must match the driver payout share used in BookingServiceImpl.completeBookingAndPay()
    private static final double DRIVER_PAYOUT_SHARE = 0.8;

    @Override
    @Transactional
    public PaymentResponse refund(Long bookingId, String reason) {

        Payment payment = paymentRepository.findByBookingId(bookingId).orElseThrow(() -> new ResourceNotFoundException("No payment found for this booking"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Only a PAID payment can be refunded (this one is " + payment.getStatus() + ")");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(Instant.now());
        payment.setRefundReason(reason);
        paymentRepository.save(payment);

        Driver driver = payment.getBooking().getDriver();
        if (driver != null) {
            double payout = payment.getAmount() * DRIVER_PAYOUT_SHARE;
            driver.setWalletBalance((driver.getWalletBalance() == null ? 0 : driver.getWalletBalance()) - payout);
            driverRepository.save(driver);
            notificationService.notify(driver.getUser().getId(), NotificationType.GENERIC,
                       "Payment refunded",
                    "A payment for booking " + payment.getBooking().getBookingReference()
                            + " was refunded: " + reason + ". Your wallet has been adjusted accordingly.",
                    bookingId);
        }

        notificationService.notify(payment.getBooking().getCustomer().getId(), NotificationType.GENERIC,
                    "Refund issued",
                "Your payment for booking " + payment.getBooking().getBookingReference()
                         + " has been refunded: " + reason,
                bookingId);

        auditLogService.log("PAYMENT_REFUNDED", "Payment", payment.getId(), reason);

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getForBooking(Long bookingId) {

        return paymentRepository.findByBookingId(bookingId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for this booking"));
    }

    private PaymentResponse toResponse(Payment p) {

        PaymentResponse r = new PaymentResponse();
        r.setId(p.getId());
        r.setBookingId(p.getBooking().getId());
        r.setAmount(p.getAmount());
        r.setCurrency(p.getCurrency());
        r.setMethod(p.getMethod());
        r.setStatus(p.getStatus());
        r.setMockTransactionRef(p.getMockTransactionRef());
        r.setPaidAt(p.getPaidAt());
        r.setRefundedAt(p.getRefundedAt());
        r.setRefundReason(p.getRefundReason());

        return r;
    }
}
