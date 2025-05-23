package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.enums.UserRole;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller for the user management web interface
 */
@Controller
@RequestMapping("/user-management")
public class UserManagementViewController {

    private final UserService userService;

    @Autowired
    public UserManagementViewController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Display the user management page
     * @param model The model to add attributes to
     * @return The user-management view or redirect to login
     */
    @GetMapping
    public String userManagement(Model model) {
        // Check if user is authenticated and has admin or vendor role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        // Check if user is admin or vendor
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/login";
        }
        
        User user = (User) principal;
        int userRole = user.getRole();
        if (userRole != UserRole.ADMIN.getValue() && userRole != UserRole.VENDOR.getValue()) {
            return "redirect:/unauthorized";
        }
        
        // User has appropriate permissions, prepare model and render view
        model.addAttribute("isAdmin", userRole == UserRole.ADMIN.getValue());
        model.addAttribute("isVendor", userRole == UserRole.VENDOR.getValue());
        
        return "user-management";
    }
    
    /**
     * Display the edit user page
     * @param userId The ID of the user to edit
     * @param model The model to add attributes to
     * @param redirectAttributes For flash attributes if redirection is needed
     * @return The edit-user view or redirect to user management
     */
    @GetMapping("/edit/{userId}")
    public String editUserPage(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        // Check if user is authenticated and has admin or vendor role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        // Check if user is admin or vendor
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/login";
        }
        
        User currentUser = (User) principal;
        int userRole = currentUser.getRole();
        if (userRole != UserRole.ADMIN.getValue() && userRole != UserRole.VENDOR.getValue()) {
            return "redirect:/unauthorized";
        }
        
        // Get the user to edit
        Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/user-management";
        }
        
        User userToEdit = userOptional.get();
        
        // Add user data and roles to the model
        model.addAttribute("user", userToEdit);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("isAdmin", userRole == UserRole.ADMIN.getValue());
        model.addAttribute("isVendor", userRole == UserRole.VENDOR.getValue());
        
        return "edit-user";
    }
    
    /**
     * Display the create user page
     * @param model The model to add attributes to
     * @return The create-user view or redirect to login/unauthorized
     */
    @GetMapping("/create")
    public String createUserPage(Model model) {
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
        
        // Add roles to the model
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("isAdmin", true);
        
        return "create-user";
    }
}
