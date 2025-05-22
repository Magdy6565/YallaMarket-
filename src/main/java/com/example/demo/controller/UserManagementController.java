package com.example.demo.controller;

import com.example.demo.dto.UserAssignmentDTO;
import com.example.demo.dto.UserCreateDTO;
import com.example.demo.dto.UserListDTO;
import com.example.demo.dto.UserUpdateRequestDTO;
import com.example.demo.model.User;
import com.example.demo.model.UserRole;
import com.example.demo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing users by administrators or vendors
 */
@RestController
@RequestMapping("/api/user-management")
public class UserManagementController {

    private final UserService userService;

    @Autowired
    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Check if the current user has admin or vendor permissions
     * @return true if the user is an admin or vendor
     */
    private boolean hasAdminOrVendorPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                User user = (User) principal;
                int role = user.getRole();
                return role == UserRole.ADMIN.getValue() || role == UserRole.VENDOR.getValue();
            }
        }
        return false;
    }

    /**
     * Get all active users
     * @return List of active users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserListDTO>> getAllUsers() {
        if (!hasAdminOrVendorPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<UserListDTO> users = userService.getAllActiveUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get users by role
     * @param roleId The role ID to filter by
     * @return List of users with the specified role
     */
    @GetMapping("/users/role/{roleId}")
    public ResponseEntity<List<UserListDTO>> getUsersByRole(@PathVariable int roleId) {
        if (!hasAdminOrVendorPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            UserRole role = UserRole.fromValue(roleId);
            List<UserListDTO> users = userService.getUsersByRole(role);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Soft delete a user
     * @param userId The ID of the user to delete
     * @return 204 No Content if successful, 404 Not Found if user not found
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long userId) {
        if (!hasAdminOrVendorPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean deleted = userService.softDeleteUser(userId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Restore a soft-deleted user
     * @param userId The ID of the user to restore
     * @return 204 No Content if successful, 404 Not Found if user not found
     */
    @PostMapping("/users/{userId}/restore")
    public ResponseEntity<Void> restoreUser(@PathVariable Long userId) {
        if (!hasAdminOrVendorPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean restored = userService.restoreUser(userId);
        return restored ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Assign a user to a vendor/store
     * @param assignment DTO containing user and vendor IDs
     * @return 204 No Content if successful, 404 Not Found if user not found
     */
    @PostMapping("/users/assign")
    public ResponseEntity<Void> assignUserToVendor(@RequestBody UserAssignmentDTO assignment) {
        if (!hasAdminOrVendorPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean assigned = userService.assignUserToVendor(assignment);
        return assigned ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Update a user
     * @param userId The ID of the user to update
     * @param updateRequest The updated user data
     * @return 200 OK with updated user DTO if successful, 404 Not Found if user not found
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequestDTO updateRequest) {
        if (!hasAdminOrVendorPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> updatedUserOptional = userService.updateUser(userId, updateRequest);
        if (updatedUserOptional.isPresent()) {
            User updatedUser = updatedUserOptional.get();
            UserListDTO userDTO = new UserListDTO();
            userDTO.setId(updatedUser.getId());
            userDTO.setUsername(updatedUser.getUsername());
            userDTO.setEmail(updatedUser.getEmail());
            userDTO.setRole(updatedUser.getRole());
            userDTO.setAddress(updatedUser.getAddress());
            userDTO.setContactInfo(updatedUser.getContactInfo());
            userDTO.setEnabled(updatedUser.isEnabled());
            userDTO.setIsDeleted(updatedUser.getDeletedAt() != null);
            
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get a user by ID
     * @param userId The ID of the user to get
     * @return 200 OK with user DTO if found, 404 Not Found if not found
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserListDTO> getUserById(@PathVariable Long userId) {
        if (!hasAdminOrVendorPermission()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserListDTO userDTO = new UserListDTO();
            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());
            userDTO.setRole(user.getRole());
            userDTO.setAddress(user.getAddress());
            userDTO.setContactInfo(user.getContactInfo());
            userDTO.setEnabled(user.isEnabled());
            userDTO.setIsDeleted(user.getDeletedAt() != null);
            
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a new user
     * @param userCreateDTO The user data to create
     * @return 201 Created with the created user DTO, 400 Bad Request if validation fails or user already exists
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        // Only admins can create users
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                User currentUser = (User) principal;
                if (currentUser.getRole() != UserRole.ADMIN.getValue()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<User> createdUserOptional = userService.createUser(userCreateDTO);
        if (createdUserOptional.isPresent()) {
            User createdUser = createdUserOptional.get();
            UserListDTO userDTO = new UserListDTO();
            userDTO.setId(createdUser.getId());
            userDTO.setUsername(createdUser.getUsername());
            userDTO.setEmail(createdUser.getEmail());
            userDTO.setRole(createdUser.getRole());
            userDTO.setAddress(createdUser.getAddress());
            userDTO.setContactInfo(createdUser.getContactInfo());
            userDTO.setEnabled(createdUser.isEnabled());
            userDTO.setIsDeleted(createdUser.getDeletedAt() != null);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
        } else {
            return ResponseEntity.badRequest().body("Username or email already exists");
        }
    }
}
