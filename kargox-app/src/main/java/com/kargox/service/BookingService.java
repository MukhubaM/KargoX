package com.kargox.service;

import com.kargox.dto.booking.*;
import com.kargox.dto.common.PageResponse;
import com.kargox.entity.BookingStatus;

public interface BookingService {

    BookingResponse createBooking(Long customerId, CreateBookingRequest request);
    BookingResponse getById(Long bookingId, Long requesterId, String requesterRole);
    BookingResponse getByReference(String reference);
    PageResponse<BookingResponse> getForCustomer(Long customerId, int page, int size);
    PageResponse<BookingResponse> getForDriver(Long driverId, int page, int size);
    PageResponse<BookingResponse> searchAll(BookingStatus status, int page, int size);
    BookingResponse updateStatus(Long bookingId, Long requesterUserId, String requesterRole, BookingStatusUpdateRequest request);
    BookingResponse cancel(Long bookingId, Long requesterUserId, String requesterRole, CancelBookingRequest request);
}
