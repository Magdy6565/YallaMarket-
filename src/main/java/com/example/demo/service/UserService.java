package com.example.demo.service;

import com.example.demo.dto.UserUpdateRequestDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
// Import PasswordEncoder if you have methods that use it (like password change or verification)
// import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;
    // Inject PasswordEncoder if needed for other methods
    // private final PasswordEncoder passwordEncoder;

    // Adjust constructor based on injected dependencies
    @Autowired
    public UserService(UserRepository userRepository /*, Optional<PasswordEncoder> passwordEncoder */) {
        this.userRepository = userRepository;
        // Initialize passwordEncoder if injected
        // this.passwordEncoder = passwordEncoder.orElse(null); // Handle optional injection if needed
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    @Transactional // Ensure atomicity for the update operation
    public Optional<User> updateUser(Long userId, UserUpdateRequestDTO updateRequest) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Update username if provided and different
            if (updateRequest.getUsername() != null && !updateRequest.getUsername().trim().isEmpty() && !user.getUsername().equals(updateRequest.getUsername())) {
                user.setUsername(updateRequest.getUsername());
            }

            if (updateRequest.getRole() != null) {
                user.setRole(updateRequest.getRole());
            }
            user.setAddress(updateRequest.getAddress());
            user.setContactInfo(updateRequest.getContactInfo());
            return Optional.of(userRepository.save(user)); // Save the updated user
        } else {
            return Optional.empty(); // User not found
        }
    }
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    public Optional<User> getUserDetails(Long userId) { // Implementation of the new method
        return userRepository.findById(userId); // Simply fetch the user by ID
    }

}
