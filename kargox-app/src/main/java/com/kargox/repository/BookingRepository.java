package com.kargox.repository;

import com.kargox.entity.Booking;
import com.kargox.entity.BookingStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String reference);

    Page<Booking> findByCustomerIdAndDeletedFalseOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Page<Booking> findByDriverIdAndDeletedFalseOrderByCreatedAtDesc(Long driverId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.deleted = false " + "AND (:status IS NULL OR b.status = :status)")
    Page<Booking> search(BookingStatus status, Pageable pageable);

    List<Booking> findByStatusAndDeletedFalse(BookingStatus status);

    long countByStatusAndDeletedFalse(BookingStatus status);
}
