package com.example.demo.controller;

import com.example.demo.dto.VendorOrderDetailsDto;
import com.example.demo.model.User;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/store/orders") // Base path for vendor order management
public class OrderStoreController {

    private final OrderService orderService;

    @Autowired
    public OrderStoreController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Helper method to get the user ID from the authenticated user
    private Long getAuthenticatedUserId() {
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



    @GetMapping
    public ResponseEntity<List<VendorOrderDetailsDto>> getAllStoreOrders() {
        Long userId = getAuthenticatedUserId(); // Get the authenticated user's ID
        List<VendorOrderDetailsDto> orders = orderService.getAllStoreOrdersInvoicesByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}