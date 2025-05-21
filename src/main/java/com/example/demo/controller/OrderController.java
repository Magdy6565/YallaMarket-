package com.example.demo.controller;

import com.example.demo.dto.OrderFilterRequest;
import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderStatusUpdateRequest;
import com.example.demo.dto.VendorOrderDetailsDto;
import com.example.demo.model.User; // Import your User model
import com.example.demo.model.Order; // Import Order model if returning updated order
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.repository.OrderRepository;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

// Import Spring Security classes for getting authenticated user
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


@RestController
@RequestMapping("/api/vendor/orders") // Base path for vendor order management
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository; // Needed for filtering logic

    @Autowired
    public OrderController(OrderRepository orderRepository , OrderService orderService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;

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


    // Endpoint to get all orders for the authenticated vendor's products
    // GET /api/vendor/orders
    @GetMapping
    public ResponseEntity<List<VendorOrderDetailsDto>> getAllVendorOrders() {
        Long vendorUserId = getAuthenticatedUserId(); // Get the authenticated user's ID
        List<VendorOrderDetailsDto> orders = orderService.getVendorOrders(vendorUserId);
        return ResponseEntity.ok(orders);
    }

    // Endpoint to update the status of a specific order
    // PUT /api/vendor/orders/{orderId}/status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest statusUpdateRequest) {

        Long vendorUserId = getAuthenticatedUserId(); // Get the authenticated user's ID
        // TODO: Add authorization check here to ensure the authenticated user is a vendor

        Optional<Order> updatedOrder = orderService.updateOrderStatus(orderId, vendorUserId, statusUpdateRequest.getStatus());

        return updatedOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 if not found or not owned
    }

    // Endpoint to filter vendor orders
    // GET /api/vendor/orders/filter
    // Filters can be passed as query parameters (e.g., ?status=pending&minTotalAmount=100)
//    @GetMapping("/filter")
//    public ResponseEntity<List<VendorOrderDetailsDto>> filterVendorOrders(
//            @ModelAttribute OrderFilterRequest filterRequest) { // Use @ModelAttribute for query params
//
//        Long vendorUserId = getAuthenticatedUserId(); // Get the authenticated user's ID
//        // TODO: Add authorization check here to ensure the authenticated user is a vendor
//
//        List<VendorOrderDetailsDto> filteredOrders = orderService.filterVendorOrders(vendorUserId, filterRequest);
//        return ResponseEntity.ok(filteredOrders);
//    }

    // You might add other endpoints here, e.g., get a single order details for the vendor
    // GET /api/vendor/orders/{orderId}
     @GetMapping("/{orderId}")
     public ResponseEntity<VendorOrderDetailsDto> getVendorOrderById(@PathVariable Long orderId) {
         Long vendorUserId = getAuthenticatedUserId();
         // TODO: Add authorization check here to ensure the authenticated user is a vendor
         Optional<Order> orderOptional = orderRepository.findOrderByOrderIdAndProductVendorId(orderId, vendorUserId);
         return orderOptional.map(VendorOrderDetailsDto::new)
                             .map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound().build());
     }


    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(
            @RequestBody OrderRequest request) {  // Inject current logged-in user
        Long userId = getAuthenticatedUserId(); // Get the authenticated user's ID

        orderService.placeOrder(request, userId);
        return ResponseEntity.ok("Order placed successfully");
    }
}