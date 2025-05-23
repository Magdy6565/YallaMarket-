package com.example.demo.controller;

import com.example.demo.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/payments")
public class PaymentViewController {

    /**
     * Display the payments page
     * 
     * @param model The model to add attributes to
     * @return The payments view or redirect to login/unauthorized
     */
    @GetMapping
    public String paymentsPage(Model model) {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }
        
        // Check if user has admin permissions
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/login";
        }
        
        User user = (User) principal;
        if (user.getRole() != 0) { // 0 is admin role
            return "redirect:/unauthorized";
        }
        
        // Pass user role to the view
        model.addAttribute("isAdmin", true);
        model.addAttribute("username", user.getUsername());
        
        return "payments";
    }
}
