package com.example.demo.controller;

import com.example.demo.dto.UserUpdateRequestDTO; // Import DTO for update
import com.example.demo.dto.UserUpdateRequestDTO;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Import all annotation types
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.TimeUnit;
import jakarta.validation.Valid; // Import validation annotation

import java.util.List; // Import List
import java.util.Optional; // Import Optional

// Import Spring Security classes for authorization check
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


@RestController
@RequestMapping("/users") // Base path for user-related endpoints (using /api/users as before)
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint to get the authenticated user's own data (from old controller, renamed slightly)
    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noStore().mustRevalidate()) // Prevents caching
                .header("Pragma", "no-cache") // For HTTP/1.0 backwards compatibility
                .header("Expires", "0") // For HTTP/1.0 backwards compatibility
                .body(currentUser);
    }

    // Endpoint to get all users (from old controller)
    // GET /api/users/
    @GetMapping("/") // Or just @GetMapping("")
    public ResponseEntity<List<User>> allUsers() {
        List <User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }


    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDTO updateRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Or UNAUTHORIZED/FORBIDDEN
        }

        User authenticatedUser = (User) principal;
        if (!authenticatedUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }
        Optional<User> updatedUserOptional = userService.updateUser(userId, updateRequest);
        return updatedUserOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


}
