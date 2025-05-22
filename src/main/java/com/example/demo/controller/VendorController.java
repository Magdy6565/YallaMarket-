package com.example.demo.controller;

import com.example.demo.dto.UserCreateDTO;
import com.example.demo.dto.UserListDTO;
import com.example.demo.dto.UserUpdateRequestDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final UserService userService;

    @Autowired
    public VendorController(UserService userService) {
        this.userService = userService;
    }

    // View all active vendors (for stores to choose from)
    @GetMapping
    public ResponseEntity<List<UserListDTO>> getAllActiveVendors() {
        List<UserListDTO> vendors = userService.getAllActiveVendors();
        return ResponseEntity.ok(vendors);
    }

    // Admin: View all vendors, including soft-deleted ones
    @GetMapping("/admin/all")
    public ResponseEntity<List<UserListDTO>> getAllVendorsForAdmin() {
        List<UserListDTO> vendors = userService.getAllVendorsIncludingDeleted();
        return ResponseEntity.ok(vendors);
    }

    // Admin: Add a new vendor
    @PostMapping("/admin")
    public ResponseEntity<UserListDTO> addVendor(@RequestBody UserCreateDTO userCreateDTO) {
        // Ensure the role is set to VENDOR (integer value 1)
        userCreateDTO.setRole(1); // VENDOR role
        Optional<User> newUser = userService.createUser(userCreateDTO);
        return newUser.map(user -> ResponseEntity.status(HttpStatus.CREATED).body(convertToUserListDTO(user)))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    // Admin: Edit an existing vendor
    @PutMapping("/admin/{vendorId}")
    public ResponseEntity<UserListDTO> updateVendor(@PathVariable Long vendorId, @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
        // If a role is provided in the update request, ensure it's VENDOR (integer value 1)
        // If other roles were permissible for a "vendor" entity through this endpoint, this logic would need adjustment.
        if (userUpdateRequestDTO.getRole() != null && userUpdateRequestDTO.getRole() != 1) { // VENDOR role
            // Consider returning a more specific error message
            return ResponseEntity.badRequest().body(null); // Or throw an IllegalArgumentException
        }
        // If role is not part of the request, it won't be updated by userService.updateUser unless explicitly handled there.
        // The current userService.updateUser updates the role if it's provided in UserUpdateRequestDTO.
        Optional<User> updatedUser = userService.updateUser(vendorId, userUpdateRequestDTO);
        return updatedUser.map(user -> ResponseEntity.ok(convertToUserListDTO(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Admin: Soft delete a vendor
    @DeleteMapping("/admin/{vendorId}")
    public ResponseEntity<Void> softDeleteVendor(@PathVariable Long vendorId) {
        boolean deleted = userService.softDeleteUser(vendorId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Admin: Restore a soft-deleted vendor
    @PostMapping("/admin/{vendorId}/restore")
    public ResponseEntity<Void> restoreVendor(@PathVariable Long vendorId) {
        boolean restored = userService.restoreUser(vendorId);
        if (restored) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Helper to convert User to UserListDTO, can be moved to a common utility or kept in UserService
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
}
