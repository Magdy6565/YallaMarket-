package com.example.demo.service; // Or com.example.demo.service.impl or com.example.demo.config

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional; // Import Optional

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory


@Service
public class OurUserDetailsService implements UserDetailsService { // Or the name of your UserDetailsService class

    // Add a logger
    private static final Logger logger = LoggerFactory.getLogger(OurUserDetailsService.class); // Use your class name here

    private final UserRepository userRepository;

    @Autowired
    public OurUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // --- Add logging at the beginning of the method ---
        logger.info("--> Attempting to load user with identifier: {}", username);
        // -------------------------------------------------

        // Based on your JwtService, 'username' here is the 'sub' claim from the JWT.
        // You set this using userDetails.getUsername() during token generation.
        // So, this 'username' parameter should match the 'username' field in your database.

        // Query the database to find the user by username
        Optional<User> userOptional = userRepository.findByUsername(username); // Or findByEmail if that's your JWT subject

        // --- Log the result of the repository call ---
        if (userOptional.isPresent()) {
            logger.info("User found in database: {}", userOptional.get().getUsername());
        } else {
            logger.warn("User NOT found in database with identifier: {}", username);
        }
        // -------------------------------------------


        // If the user is not found, throw UsernameNotFoundException
        return userOptional
                .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + username)); // Keep the exception throwing
    }
}
