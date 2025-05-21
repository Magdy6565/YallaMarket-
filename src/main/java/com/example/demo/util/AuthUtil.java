package com.example.demo.util;

import com.example.demo.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    // Helper method to get the user ID from the authenticated user
    public static Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User authenticatedUser = (User) principal;
            if (authenticatedUser.getId() == null) {
                throw new IllegalStateException("Authenticated user has no ID");
            }
            return authenticatedUser.getId(); // Return the user's ID (Long)
        } else {
            throw new IllegalStateException("Authenticated principal is not a recognized User type");
        }
    }
}
