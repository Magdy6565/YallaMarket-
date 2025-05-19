package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class WebViewController {

    private final ProductService productService;

    @Autowired
    public WebViewController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * Display the login page
     * 
     * @return The login view name
     */
    @GetMapping("/login")
    public String login() {
        // If user is already logged in, redirect to products page
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/products";
        }
        return "login";
    }
    
    /**
     * Display the products page
     * 
     * @param model The model to add attributes to
     * @return The products view or redirect to login
     */
    @GetMapping("/products")
    public String products(Model model) {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && 
                                 authentication.isAuthenticated() && 
                                 !authentication.getName().equals("anonymousUser");
        
        if (!isAuthenticated) {
            return "redirect:/login";
        }
        
        // Get user ID from authentication
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Get all products for the user
        List<Product> products = productService.getAllProductsForUser(userId);
        model.addAttribute("products", products);
        
        return "products";
    }
    
    /**
     * Display the product details page
     * 
     * @param productId The ID of the product
     * @param model The model to add attributes to
     * @return The product-details view or redirect to login or products
     */
    @GetMapping("/products/{productId}")
    public String productDetails(@PathVariable Long productId, Model model) {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && 
                                 authentication.isAuthenticated() && 
                                 !authentication.getName().equals("anonymousUser");
        
        if (!isAuthenticated) {
            return "redirect:/login";
        }
        
        // Get user ID from authentication
        Long userId = getUserIdFromAuthentication(authentication);
        
        // Get product details
        Optional<Product> product = productService.getProductByIdForUser(productId, userId);
        
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "product-details";
        } else {
            return "redirect:/products";
        }
    }
    
    /**
     * Home page redirect to login or products
     * 
     * @return The redirect URL
     */
    @GetMapping("/")
    public String home() {
        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && 
                                 authentication.isAuthenticated() && 
                                 !authentication.getName().equals("anonymousUser");
        
        if (isAuthenticated) {
            return "redirect:/products";
        } else {
            return "redirect:/login";
        }
    }
    
    /**
     * Helper method to get user ID from authentication
     * 
     * @param authentication The authentication object
     * @return The user ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("Authentication is null");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof com.example.demo.model.User) {
            return ((com.example.demo.model.User) principal).getId();
        } else {
            // For testing or when not fully authenticated
            return 1L; // Default user ID for testing
        }
    }
}
