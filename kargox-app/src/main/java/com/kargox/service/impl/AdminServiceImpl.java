package com.kargox.service.impl;

import com.kargox.dto.admin.DashboardStatsResponse;
import com.kargox.dto.admin.UserAdminResponse;
import com.kargox.dto.common.PageResponse;
import com.kargox.entity.BookingStatus;
import com.kargox.entity.Role;
import com.kargox.entity.User;
import com.kargox.exception.ResourceNotFoundException;
import com.kargox.repository.BookingRepository;
import com.kargox.repository.DriverRepository;
import com.kargox.repository.PaymentRepository;
import com.kargox.repository.UserRepository;
import com.kargox.entity.DriverStatus;
import com.kargox.entity.PaymentStatus;
import com.kargox.service.AdminService;
import com.kargox.service.AuditLogService;
import com.kargox.service.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final DriverRepository driverRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    @Override
    public PageResponse<UserAdminResponse> searchUsers(Role role, String search, int page, int size) {

        String searchPattern = (search == null || search.isBlank()) ? null : "%" + search.toLowerCase() + "%";
        Page<User> result = userRepository.search(role, searchPattern, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return PageResponse.from(result.map(this::toResponse));
    }

    @Override
    @Transactional
    public UserAdminResponse setUserActive(Long userId, boolean active) {

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setActive(active);
        userRepository.save(user);
        auditLogService.log(active ? "USER_REACTIVATED" : "USER_SUSPENDED", "User", userId, null);

        emailService.sendBookingNotificationEmail(user.getEmail(),
                active ? "KargoX - Your account has been reactivated" : "KargoX | Your account has been suspended",
                active
                        ? "Your KargoX account is active again. You can sign back in right away."
                        : "Your KargoX account has been suspended by an administrator. "
                        + "If you believe this is a mistake, please contact support.");

        return toResponse(user);
    }

    @Override
    public DashboardStatsResponse getDashboardStats() {

        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.search(Role.CUSTOMER, null, PageRequest.of(0, 1)).getTotalElements();
        long totalDrivers = userRepository.search(Role.DRIVER, null, PageRequest.of(0, 1)).getTotalElements();
        long activeDrivers = driverRepository.findAvailableWithLocation(DriverStatus.ONLINE).size();

        long totalBookings = bookingRepository.count();
        long completed = bookingRepository.countByStatusAndDeletedFalse(BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByStatusAndDeletedFalse(BookingStatus.CANCELLED);

        long inProgress = Arrays.stream(new BookingStatus[]{
                        BookingStatus.DRIVER_ASSIGNED, BookingStatus.DRIVER_EN_ROUTE, BookingStatus.DRIVER_ARRIVED,
                        BookingStatus.GOODS_COLLECTED, BookingStatus.IN_TRANSIT})
                .mapToLong(s -> bookingRepository.countByStatusAndDeletedFalse(s))
                .sum();

        double revenue = paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .mapToDouble(p -> p.getAmount() == null ? 0 : p.getAmount())
                .sum();

        Map<String, Long> byStatus = new LinkedHashMap<>();

        for (BookingStatus status : BookingStatus.values()) {
            byStatus.put(status.name(), bookingRepository.countByStatusAndDeletedFalse(status));
        }

        return new DashboardStatsResponse(totalUsers, totalCustomers, totalDrivers, activeDrivers, totalBookings, completed, cancelled, inProgress, revenue, byStatus);
    }

    private UserAdminResponse toResponse(User u) {

        UserAdminResponse r = new UserAdminResponse();
        r.setId(u.getId());
        r.setFullName(u.getFullName());
        r.setEmail(u.getEmail());
        r.setPhoneNumber(u.getPhoneNumber());
        r.setRole(u.getRole());
        r.setActive(u.isActive());
        r.setEmailVerified(u.isEmailVerified());
        r.setAverageRating(u.getAverageRating());
        r.setCreatedAt(u.getCreatedAt());
        r.setLastLoginAt(u.getLastLoginAt());

        return r;
    }
}
