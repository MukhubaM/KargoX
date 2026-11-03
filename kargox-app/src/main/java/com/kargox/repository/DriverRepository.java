package com.kargox.repository;

import com.kargox.entity.Driver;
import com.kargox.entity.DriverStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUserIdAndDeletedFalse(Long userId);

    @Query("SELECT d FROM Driver d WHERE d.status = :status AND d.deleted = false " + "AND d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL")
    List<Driver> findAvailableWithLocation(DriverStatus status);
}
