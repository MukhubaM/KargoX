package com.kargox.controller;

import com.kargox.dto.admin.*;
import com.kargox.dto.common.ApiResponse;
import com.kargox.dto.common.PageResponse;
import com.kargox.dto.common.PaymentResponse;
import com.kargox.dto.common.RefundRequest;
import com.kargox.dto.booking.BookingResponse;
import com.kargox.entity.BookingStatus;
import com.kargox.entity.Role;
import com.kargox.service.AdminService;
import com.kargox.service.BookingService;
import com.kargox.service.PaymentService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Platform administration: users, bookings, analytics")
public class AdminController {

    private final AdminService adminService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<UserAdminResponse>>> searchUsers(@RequestParam(required = false)
                                                                                    Role role,
                                                                                    @RequestParam(required = false)
                                                                                    String search,
                                                                                    @RequestParam(defaultValue = "0")
                                                                                    int page,
                                                                                    @RequestParam(defaultValue = "20")
                                                                                    int size) {

        return ResponseEntity.ok(ApiResponse.ok(adminService.searchUsers(role, search, page, size)));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserAdminResponse>> setUserStatus(@PathVariable
                                                                        Long id,
                                                                        @Valid @RequestBody
                                                                        UserStatusUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(adminService.setUserActive(id, request.getActive())));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> searchBookings(@RequestParam(required = false)
                                                                                     BookingStatus status,
                                                                                     @RequestParam(defaultValue = "0")
                                                                                     int page,
                                                                                     @RequestParam(defaultValue = "20")
                                                                                     int size) {

        return ResponseEntity.ok(ApiResponse.ok(bookingService.searchAll(status, page, size)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> dashboard() {

        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    @GetMapping("/bookings/{bookingId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable Long bookingId) {

        return ResponseEntity.ok(ApiResponse.ok(paymentService.getForBooking(bookingId)));
    }

    @PostMapping("/bookings/{bookingId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(@PathVariable Long bookingId,
                                                               @Valid @RequestBody RefundRequest request) {

        return ResponseEntity.ok(ApiResponse.ok("Refund issued", paymentService.refund(bookingId, request.getReason())));
    }
}
