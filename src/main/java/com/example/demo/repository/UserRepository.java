package com.example.demo.repository;


import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find user by email
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByContactInfo(String contactInfo); // Add this if contactInfo is unique}

    // Find user by the verification code they would enter
    Optional<User> findByVerificationCode(String verificationCode);

    // Find all active users (not soft-deleted)
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllActive();

    // Find all active users by role
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    List<User> findAllActiveByRole(@Param("role") int role);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findAllByRole(@Param("role") int role);

    // Find all users by role
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.deletedAt IS NULL")
    List<User> findAllByRoleAndActive(@Param("role") int role);

}