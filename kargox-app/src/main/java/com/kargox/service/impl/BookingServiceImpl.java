package com.kargox.service.impl;

import com.kargox.dto.booking.*;
import com.kargox.dto.common.PageResponse;
import com.kargox.entity.*;
import com.kargox.exception.BadRequestException;
import com.kargox.exception.InvalidBookingStateException;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.BookingRepository;
import com.kargox.repository.DriverRepository;
import com.kargox.repository.PaymentRepository;
import com.kargox.repository.UserRepository;
import com.kargox.service.*;
import com.kargox.util.CodeGenerator;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// This orchestrates the full booking lifecycle in the system
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PaymentRepository paymentRepository;
    private final FareCalculationService fareCalculationService;
    private final DriverMatchingService driverMatchingService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Set<BookingStatus> CANCELLABLE_STATES = Set.of(
            BookingStatus.REQUESTED, BookingStatus.SEARCHING_DRIVER, BookingStatus.DRIVER_ASSIGNED,
            BookingStatus.DRIVER_EN_ROUTE, BookingStatus.DRIVER_ARRIVED, BookingStatus.NO_DRIVER_FOUND);

    @Override
    @Transactional
    public BookingResponse createBooking(Long customerId, CreateBookingRequest request) {

        User customer = userRepository.findById(customerId).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        FareEstimateResponse estimate = fareCalculationService.estimate(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDestinationLatitude(), request.getDestinationLongitude(),
                request.getGoodsItems());

        Booking booking = new Booking();
        booking.setBookingReference(CodeGenerator.bookingReference());
        booking.setCustomer(customer);
        booking.setPickupAddress(request.getPickupAddress());
        booking.setPickupLatitude(request.getPickupLatitude());
        booking.setPickupLongitude(request.getPickupLongitude());
        booking.setDestinationAddress(request.getDestinationAddress());
        booking.setDestinationLatitude(request.getDestinationLatitude());
        booking.setDestinationLongitude(request.getDestinationLongitude());
        booking.setRecommendedVehicleType(estimate.getRecommendedVehicleType());
        booking.setDeliveryInstructions(request.getDeliveryInstructions());
        booking.setEstimatedDistanceKm(estimate.getDistanceKm());
        booking.setEstimatedDurationMinutes(estimate.getEstimatedDurationMinutes());
        booking.setEstimatedFare(estimate.getEstimatedFare());
        booking.setScheduled(request.isScheduled());
        booking.setScheduledFor(request.getScheduledFor());
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setPickupPin(CodeGenerator.numericPin());
        booking.setDeliveryOtp(CodeGenerator.numericPin());

        request.getGoodsItems().forEach(dto -> {
            GoodsItem item = new GoodsItem();
            item.setBooking(booking);
            item.setName(dto.getName());
            item.setCategory(dto.getCategory());
            item.setSizeTier(dto.getSizeTier());
            item.setQuantity(dto.getQuantity());
            item.setFragile(dto.isFragile());
            item.setPhotoUrl(dto.getPhotoUrl());
            booking.getGoodsItems().add(item);
        });

        bookingRepository.save(booking);
        auditLogService.log("BOOKING_CREATED", "Booking", booking.getId(), booking.getBookingReference());

        // This immediately broadcast to the nearby drivers unless it is scheduled for later
        if (!booking.isScheduled()) {
            driverMatchingService.findAndNotifyNearbyDrivers(booking);
        }

        return toResponse(bookingRepository.findById(booking.getId()).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getById(Long bookingId, Long requesterId, String requesterRole) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        assertParticipant(booking, requesterId, requesterRole);

        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getByReference(String reference) {

        return toResponse(bookingRepository.findByBookingReference(reference).orElseThrow(() -> new ResourceNotFoundException("Booking not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getForCustomer(Long customerId, int page, int size) {

        Page<Booking> result = bookingRepository.findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(customerId, PageRequest.of(page, size));

        return PageResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getForDriver(Long driverId, int page, int size) {

        Page<Booking> result = bookingRepository.findByDriverIdAndDeletedFalseOrderByCreatedAtDesc(driverId, PageRequest.of(page, size));

        return PageResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> searchAll(BookingStatus status, int page, int size) {

        Page<Booking> result = bookingRepository.search(status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return PageResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional
    public BookingResponse updateStatus(Long bookingId, Long requesterUserId, String requesterRole, BookingStatusUpdateRequest request) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        assertParticipant(booking, requesterUserId, requesterRole);

        BookingStatus target = request.getStatus();
        validateTransition(booking, target, requesterRole);

        switch (target) {
            case DRIVER_EN_ROUTE -> booking.setStatus(target);
            case DRIVER_ARRIVED -> {
                booking.setStatus(target);
                booking.setDriverArrivedAt(Instant.now());
                notify(booking.getCustomer().getId(), NotificationType.DRIVER_ARRIVED, "Driver has arrived", "Your driver is at the pickup location", booking.getId());
            }
            case GOODS_COLLECTED -> {
                requireCode(request.getVerificationCode(), booking.getPickupPin(), "pickup PIN");
                booking.setStatus(target);
                booking.setGoodsCollectedAt(Instant.now());
                booking.setPickupPhotoUrl(request.getPhotoUrl());
                booking.setPickupSignatureUrl(request.getSignatureUrl());
                notify(booking.getCustomer().getId(), NotificationType.GOODS_COLLECTED, "Goods collected", "Your goods are on their way", booking.getId());
            }
            case IN_TRANSIT -> booking.setStatus(target);
            case DELIVERED -> {
                requireCode(request.getVerificationCode(), booking.getDeliveryOtp(), "delivery OTP");
                booking.setStatus(target);
                booking.setDeliveredAt(Instant.now());
                booking.setDeliveryPhotoUrl(request.getPhotoUrl());
                booking.setDeliverySignatureUrl(request.getSignatureUrl());
                notify(booking.getCustomer().getId(), NotificationType.DELIVERY_COMPLETED, "Delivered", "Your goods have been delivered", booking.getId());
            }
            case COMPLETED -> completeBookingAndPay(booking);
            default -> throw new InvalidBookingStateException("Unsupported status transition to " + target);
        }

        bookingRepository.save(booking);
        messagingTemplate.convertAndSend("/topic/bookings/" + booking.getId(), booking.getStatus());
        auditLogService.log("BOOKING_STATUS_" + target, "Booking", booking.getId(), null);

        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancel(Long bookingId, Long requesterUserId, String requesterRole, CancelBookingRequest request) {

        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        assertParticipant(booking, requesterUserId, requesterRole);

        if (!CANCELLABLE_STATES.contains(booking.getStatus())) {
            throw new InvalidBookingStateException("Booking can no longer be cancelled at status " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());
        booking.setCancellationReason(request.getReason());
        booking.setCancelledByRole(requesterRole);

        if (booking.getDriver() != null) {
            Driver driver = booking.getDriver();
            driver.setStatus(DriverStatus.ONLINE);
            driverRepository.save(driver);
        }

        bookingRepository.save(booking);
        messagingTemplate.convertAndSend("/topic/bookings/" + booking.getId(), "CANCELLED");
        auditLogService.log("BOOKING_CANCELLED", "Booking", booking.getId(), request.getReason());

        Long notifyUserId = requesterRole.equals("CUSTOMER") && booking.getDriver() != null
                ? booking.getDriver().getUser().getId()
                : booking.getCustomer().getId();
        notify(notifyUserId, NotificationType.BOOKING_CANCELLED, "Booking cancelled", request.getReason(), booking.getId());

        return toResponse(booking);
    }

    // The helpers

    private void completeBookingAndPay(Booking booking) {

        if (booking.getStatus() != BookingStatus.DELIVERED) {
            throw new InvalidBookingStateException("Booking must be DELIVERED before it can be completed");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(Instant.now());
        booking.setFinalFare(booking.getEstimatedFare());

        // Mock payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getFinalFare());
        payment.setMethod(PaymentMethod.MOCK_CARD);
        payment.setStatus(PaymentStatus.PAID);
        payment.setMockTransactionRef("PAYMENT-" + booking.getBookingReference());
        payment.setPaidAt(Instant.now());
        paymentRepository.save(payment);

        if (booking.getDriver() != null) {
            Driver driver = booking.getDriver();
            driver.setStatus(DriverStatus.ONLINE);
            driver.setCompletedDeliveries((driver.getCompletedDeliveries() == null ? 0 : driver.getCompletedDeliveries()) + 1);
            driver.setWalletBalance((driver.getWalletBalance() == null ? 0 : driver.getWalletBalance()) + booking.getFinalFare() * 0.8);
            driverRepository.save(driver);
            notify(driver.getUser().getId(), NotificationType.PAYMENT_CONFIRMED, "Delivery completed", "Payment received for booking " + booking.getBookingReference(), booking.getId());
        }

        notify(booking.getCustomer().getId(), NotificationType.PAYMENT_CONFIRMED, "Payment confirmed", "Thanks for using KargoX! Your delivery is complete.", booking.getId());
    }

    private void validateTransition(Booking booking, BookingStatus target, String requesterRole) {

        boolean isDriver = "DRIVER".equals(requesterRole);
        boolean isCustomer = "CUSTOMER".equals(requesterRole);
        boolean isAdmin = "ADMIN".equals(requesterRole);

        BookingStatus current = booking.getStatus();

        boolean valid = switch (target) {

            case DRIVER_EN_ROUTE -> current == BookingStatus.DRIVER_ASSIGNED && (isDriver || isAdmin);
            case DRIVER_ARRIVED -> current == BookingStatus.DRIVER_EN_ROUTE && (isDriver || isAdmin);
            case GOODS_COLLECTED -> current == BookingStatus.DRIVER_ARRIVED && (isDriver || isAdmin);
            case IN_TRANSIT -> current == BookingStatus.GOODS_COLLECTED && (isDriver || isAdmin);
            case DELIVERED -> current == BookingStatus.IN_TRANSIT && (isDriver || isAdmin);
            case COMPLETED -> current == BookingStatus.DELIVERED && (isCustomer || isDriver || isAdmin);
            default -> false;
        };

        if (!valid) {
            throw new InvalidBookingStateException(
                    "Cannot transition booking from " + current + " to " + target + " as role " + requesterRole);
        }
    }

    private void requireCode(String provided, String expected, String label) {

        if (provided == null || !provided.equals(expected)) {
            throw new BadRequestException("Invalid " + label + " provided");
        }
    }

    private void assertParticipant(Booking booking, Long requesterId, String requesterRole) {

        if ("ADMIN".equals(requesterRole)) {

            return;
        }

        boolean isCustomer = booking.getCustomer().getId().equals(requesterId);
        boolean isDriver = booking.getDriver() != null && booking.getDriver().getUser().getId().equals(requesterId);

        if (!isCustomer && !isDriver) {
            throw new AccessDeniedException("You do not have access to this booking");
        }
    }

    private void notify(Long userId, NotificationType type, String title, String message, Long bookingId) {

        notificationService.notify(userId, type, title, message, bookingId);
    }

    private BookingResponse toResponse(Booking b) {

        BookingResponse r = new BookingResponse();
        r.setId(b.getId());
        r.setBookingReference(b.getBookingReference());
        r.setStatus(b.getStatus());
        r.setCustomerId(b.getCustomer().getId());
        r.setCustomerName(b.getCustomer().getFullName());

        if (b.getDriver() != null) {
            r.setDriverId(b.getDriver().getId());
            r.setDriverName(b.getDriver().getUser().getFullName());
            r.setDriverPhone(b.getDriver().getUser().getPhoneNumber());
            r.setDriverRating(b.getDriver().getAverageRating());
            r.setDriverLatitude(b.getDriver().getCurrentLatitude());
            r.setDriverLongitude(b.getDriver().getCurrentLongitude());
        }
        if (b.getVehicle() != null) {
            r.setVehicleId(b.getVehicle().getId());
            r.setVehicleType(b.getVehicle().getType());
            r.setVehicleRegistration(b.getVehicle().getRegistrationNumber());
        }

        r.setPickupAddress(b.getPickupAddress());
        r.setPickupLatitude(b.getPickupLatitude());
        r.setPickupLongitude(b.getPickupLongitude());
        r.setDestinationAddress(b.getDestinationAddress());
        r.setDestinationLatitude(b.getDestinationLatitude());
        r.setDestinationLongitude(b.getDestinationLongitude());
        r.setRecommendedVehicleType(b.getRecommendedVehicleType());

        r.setGoodsItems(b.getGoodsItems().stream().map(this::toGoodsDto).collect(Collectors.toList()));
        r.setDeliveryInstructions(b.getDeliveryInstructions());
        r.setEstimatedDistanceKm(b.getEstimatedDistanceKm());
        r.setEstimatedDurationMinutes(b.getEstimatedDurationMinutes());
        r.setEstimatedFare(b.getEstimatedFare());
        r.setFinalFare(b.getFinalFare());
        r.setScheduled(b.isScheduled());
        r.setScheduledFor(b.getScheduledFor());
        r.setPickupPin(b.getPickupPin());
        r.setDeliveryOtp(b.getDeliveryOtp());
        r.setCreatedAt(b.getCreatedAt());
        r.setDriverAssignedAt(b.getDriverAssignedAt());
        r.setDriverArrivedAt(b.getDriverArrivedAt());
        r.setGoodsCollectedAt(b.getGoodsCollectedAt());
        r.setDeliveredAt(b.getDeliveredAt());
        r.setCompletedAt(b.getCompletedAt());
        r.setCancelledAt(b.getCancelledAt());
        r.setCancellationReason(b.getCancellationReason());

        return r;
    }

    private GoodsItemDto toGoodsDto(GoodsItem item) {

        GoodsItemDto dto = new GoodsItemDto();
        dto.setName(item.getName());
        dto.setCategory(item.getCategory());
        dto.setSizeTier(item.getSizeTier());
        dto.setQuantity(item.getQuantity());
        dto.setFragile(item.isFragile());
        dto.setPhotoUrl(item.getPhotoUrl());

        return dto;
    }
}
