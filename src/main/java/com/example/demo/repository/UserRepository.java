package com.example.demo.repository;


import com.example.demo.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    // Find user by email
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByContactInfo(String contactInfo); // Add this if contactInfo is unique}
    Optional<User> findByVerificationCode(String verificationCode);
}