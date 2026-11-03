package com.kargox.dto.booking;

import com.kargox.entity.BookingStatus;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingStatusUpdateRequest {

    @NotNull
    private BookingStatus status;

    // required for goods collected or delivered(pickup PIN/delivery OTP)
    private String verificationCode;
    private String photoUrl;
    private String signatureUrl;
}
