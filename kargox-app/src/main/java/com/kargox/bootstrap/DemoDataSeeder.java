package com.kargox.bootstrap;

import com.kargox.entity.*;
import com.kargox.repository.DriverRepository;
import com.kargox.repository.UserRepository;
import com.kargox.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// This is for seeding a default admin account and a demo drivers so the API(and the frontend built against it) has something to work with(This is for development phases)
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        if (userRepository.count() > 0) {

            return;
        }

        User admin = new User();
        admin.setFullName("CodingWithCuba");
        admin.setEmail("admin@kargox.com");
        admin.setPasswordHash(passwordEncoder.encode("Admin@12345"));
        admin.setRole(Role.ADMIN);
        admin.setEmailVerified(true);
        userRepository.save(admin);

        User driverUser = new User();
        driverUser.setFullName("Sipho Nkosi");
        driverUser.setEmail("driver@kargox.com");
        driverUser.setPasswordHash(passwordEncoder.encode("Driver@12345"));
        driverUser.setPhoneNumber("+27821234567");
        driverUser.setRole(Role.DRIVER);
        driverUser.setEmailVerified(true);
        userRepository.save(driverUser);

        Driver driver = new Driver();
        driver.setUser(driverUser);
        driver.setLicenseNumber("LIC-2024-0001");
        driver.setStatus(DriverStatus.ONLINE);
        driver.setDocumentsVerified(true);
        driver.setCurrentLatitude(-26.2041);
        driver.setCurrentLongitude(28.0473);
        driverRepository.save(driver);

        Vehicle vehicle = new Vehicle();
        vehicle.setDriver(driver);
        vehicle.setType(VehicleType.PICKUP_TRUCK);
        vehicle.setMake("Toyota");
        vehicle.setModel("Hilux");
        vehicle.setRegistrationNumber("KX 01 GP");
        vehicle.setColor("White");
        vehicle.setMaxWeightKg(800.0);
        vehicle.setDocumentsVerified(true);
        vehicleRepository.save(vehicle);

        User customerUser = new User();
        customerUser.setFullName("Thandi Mokoena");
        customerUser.setEmail("customer@kargox.com");
        customerUser.setPasswordHash(passwordEncoder.encode("Customer@12345"));
        customerUser.setPhoneNumber("+27827654321");
        customerUser.setRole(Role.CUSTOMER);
        customerUser.setEmailVerified(true);
        userRepository.save(customerUser);

        log.info("Demo data seeded: admin@kargox.com / driver@kargox.com / customer@kargox.com (passwords: Role@12345)");
    }
}
