package com.example.demo.controller;

import com.example.demo.dto.CurrentUserDto;
import com.example.demo.model.User;
import com.example.demo.util.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    /**
     * Returns basic information about the currently logged in user
     * @return CurrentUserDto object containing the user's ID and other basic info
     */
    @GetMapping("/current")
    public ResponseEntity<CurrentUserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                User currentUser = (User) principal;
                CurrentUserDto userDto = new CurrentUserDto();
                userDto.setId(currentUser.getId());
                userDto.setUsername(currentUser.getUsername());
                userDto.setEmail(currentUser.getEmail());
                userDto.setRole(currentUser.getRole());
                return ResponseEntity.ok(userDto);
            } else {
                // If not a User instance, use AuthUtil as a fallback
                Long userId = AuthUtil.getAuthenticatedUserId();
                CurrentUserDto userDto = new CurrentUserDto();
                userDto.setId(userId);
                return ResponseEntity.ok(userDto);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).build(); // Internal Server Error
        }
    }
}
