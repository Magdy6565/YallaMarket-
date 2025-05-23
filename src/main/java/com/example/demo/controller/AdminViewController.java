package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for admin interface pages
 */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    /**
     * Display the user management page for admins
     * 
     * @param model The model to add attributes to
     * @return The user-management view or redirect to login/unauthorized
     */    @GetMapping("/users")
    public String userManagementPage(Model model) {
        // Check if user is authenticated and has admin role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        // Check if user is admin
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/login";
        }
        
        User currentUser = (User) principal;
        int userRole = currentUser.getRole();
        if (userRole != UserRole.ADMIN.getValue()) {
            return "redirect:/unauthorized";
        }
        
        // Add username to the model for display in the UI
        model.addAttribute("username", currentUser.getUsername());
        
        return "admin-user-management";
    }
    
    /**
     * Display the product management page for admins
     * 
     * @param model The model to add attributes to
     * @return The product-management view or redirect to login/unauthorized
     */
    @GetMapping("/products")
    public String productManagementPage(Model model) {
        // Check if user is authenticated and has admin role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        // Check if user is admin
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/login";
        }
        
        User currentUser = (User) principal;
        int userRole = currentUser.getRole();
        if (userRole != UserRole.ADMIN.getValue()) {
            return "redirect:/unauthorized";
        }
        
        // Add username to the model for display in the UI
        model.addAttribute("username", currentUser.getUsername());
        
        return "admin-product-management";
    }
}
