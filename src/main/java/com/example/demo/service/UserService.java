package com.example.demo.service;

import com.example.demo.dto.UserAssignmentDTO;
import com.example.demo.dto.UserCreateDTO;
import com.example.demo.dto.UserListDTO;
import com.example.demo.dto.UserUpdateRequestDTO;
import com.example.demo.model.User;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
    
    /**
     * Get all active users (not soft deleted)
     * @return List of active users
     */
    public List<UserListDTO> getAllActiveUsers() {
        return userRepository.findAllActive().stream()
                .map(this::convertToUserListDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all users with a specific role
     * @param role The role to filter by
     * @return List of users with the specified role
     */
    public List<UserListDTO> getUsersByRole(UserRole role) {
        return userRepository.findAllByRoleAndActive(role.getValue()).stream()
                .map(this::convertToUserListDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all active vendors
     * @return List of active vendors
     */
    public List<UserListDTO> getAllActiveVendors() {
        return userRepository.findAllActiveByRole(UserRole.VENDOR.getValue()).stream()
                .map(this::convertToUserListDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all vendors, including soft-deleted ones
     * @return List of all vendors
     */
    public List<UserListDTO> getAllVendorsIncludingDeleted() {
        return userRepository.findAllByRole(UserRole.VENDOR.getValue()).stream() // Assuming you add findAllByRole to UserRepository
                .map(this::convertToUserListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Soft delete a user by setting the deletedAt timestamp
     * @param userId The ID of the user to delete
     * @return true if the user was deleted, false if not found
     */
    @Transactional
    public boolean softDeleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    /**
     * Restore a soft-deleted user
     * @param userId The ID of the user to restore
     * @return true if the user was restored, false if not found
     */
    @Transactional
    public boolean restoreUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setDeletedAt(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    /**
     * Assign a user to a vendor/store
     * Future implementation may need to track which vendor manages which users
     * @param assignmentDTO DTO containing user and vendor IDs
     * @return true if the assignment was successful, false otherwise
     */    @Transactional
    public boolean assignUserToVendor(UserAssignmentDTO assignmentDTO) {
        // This is a placeholder implementation that would need to be adjusted
        // based on how user-vendor relationships are tracked in your system
        Optional<User> userOptional = userRepository.findById(assignmentDTO.getUserId());
        if (userOptional.isPresent()) {
            // User exists, so assignment can proceed
            // In a real implementation, you would set a relationship between
            // the user and vendor/store in the database
            return true;
        }
        return false;
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
    
    /**
     * Create a new user
     * @param userCreateDTO The DTO containing user creation data
     * @return Optional containing the created user, or empty if user couldn't be created
     */
    @Transactional
    public Optional<User> createUser(UserCreateDTO userCreateDTO) {
        // Check if username or email already exists
        if (userRepository.findByUsername(userCreateDTO.getUsername()).isPresent()) {
            return Optional.empty(); // Username already exists
        }
        
        if (userRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            return Optional.empty(); // Email already exists
        }
          // Create new user
        User newUser = new User();
        newUser.setUsername(userCreateDTO.getUsername());
        newUser.setEmail(userCreateDTO.getEmail());
        
        // Hash the password before storing
        newUser.setPassword(passwordEncoder.encode(userCreateDTO.getPassword()));
        
        newUser.setRole(userCreateDTO.getRole());
        newUser.setAddress(userCreateDTO.getAddress());
        newUser.setContactInfo(userCreateDTO.getContactInfo());
        newUser.setEnabled(true);
        
        // Save and return the new user
        return Optional.of(userRepository.save(newUser));
    }

    /**
     * Convert a User entity to UserListDTO
     * @param user The user entity
     * @return The DTO with necessary fields for display
     */
    private UserListDTO convertToUserListDTO(User user) {
        UserListDTO dto = new UserListDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setAddress(user.getAddress());
        dto.setContactInfo(user.getContactInfo());
        dto.setEnabled(user.isEnabled());
        dto.setIsDeleted(user.getDeletedAt() != null);
        return dto;
    }

    /**
     * Get a paginated list of active users
     * @param pageNumber The page number to retrieve (0-indexed)
     * @param pageSize The number of users per page
     * @return A page of active users
     */
    public Page<UserListDTO> getPaginatedActiveUsers(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> userPage = userRepository.findAllActive(pageable);
        
        // Convert to DTO page
        List<UserListDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToUserListDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(userDTOs, pageable, userPage.getTotalElements());
    }
    
    /**
     * Get paginated users with filters
     * @param page The page number (0-based)
     * @param size The page size
     * @param roleFilter The role to filter by (-1 for all roles)
     * @param statusFilter The status to filter by ("all", "active", "inactive", "deleted")
     * @return Page of users matching the filters
     */
    public Page<UserListDTO> getPaginatedUsers(int page, int size, int roleFilter, String statusFilter) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findUsersByFilters(roleFilter, statusFilter, pageable);
        
        return userPage.map(this::convertToUserListDTO);
    }
    
    /**
     * Get all active users with pagination
     * @param page The page number (0-based)
     * @param size The page size
     * @return Page of active users
     */
    public Page<UserListDTO> getAllActiveUsersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllActive(pageable);
        
        return userPage.map(this::convertToUserListDTO);
    }
    
    /**
     * Get users by role with pagination
     * @param role The role to filter by
     * @param page The page number (0-based)
     * @param size The page size
     * @return Page of users with the specified role
     */
    public Page<UserListDTO> getUsersByRolePaginated(UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAllActiveByRole(role.getValue(), pageable);
        
        return userPage.map(this::convertToUserListDTO);
    }
}
