package com.kargox.repository;

import com.kargox.entity.Role;
import com.kargox.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndDeletedFalse(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.deleted = false " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:searchPattern IS NULL OR LOWER(u.fullName) LIKE :searchPattern " +
            "     OR LOWER(u.email) LIKE :searchPattern)")
    Page<User> search(Role role, String searchPattern, Pageable pageable);
}
