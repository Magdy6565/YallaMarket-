package com.example.demo.controller;

import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderStatusUpdateRequest;
import com.example.demo.dto.VendorOrderDetailsDto;
import com.example.demo.model.Order;
import com.example.demo.model.OrderStatus;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderService;
import com.example.demo.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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

    //-------------------------------------------------------------
    // GET /api/vendor/orders
    @GetMapping
    public ResponseEntity<List<VendorOrderDetailsDto>> getAllVendorOrders() {
        Long vendorUserId = AuthUtil.getAuthenticatedUserId(); // Get the authenticated user's ID
        List<VendorOrderDetailsDto> orders = orderService.getVendorOrders(vendorUserId);
        return ResponseEntity.ok(orders);
    }
    //-------------------------------------------------------------
    // PUT /api/vendor/orders/{orderId}/status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest statusUpdateRequest) {

        Long vendorUserId = AuthUtil.getAuthenticatedUserId(); // Get the authenticated user's ID
        // TODO: Add authorization check here to ensure the authenticated user is a vendor

        Optional<Order> updatedOrder = orderService.updateOrderStatus(orderId, vendorUserId, statusUpdateRequest.getStatus());

        return updatedOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 if not found or not owned
    }
    //-------------------------------------------------------------
    @GetMapping("/filter")
    public ResponseEntity<List<VendorOrderDetailsDto>> getFilteredVendorOrders(@RequestParam(required = false) Integer status) {
        Long vendorUserId = AuthUtil.getAuthenticatedUserId();
        if (status == null) {
            return ResponseEntity.ok(orderService.getVendorOrders(vendorUserId));
        }

        try {
            OrderStatus orderStatus = OrderStatus.fromStatusCode(status);
            List<VendorOrderDetailsDto> filteredOrders = orderService.getOrdersByStatus(orderStatus);
            return ResponseEntity.ok(filteredOrders);
        } catch (IllegalArgumentException e) {
            // Handle invalid status code gracefully (e.g., return 400 Bad Request)
            return ResponseEntity.badRequest().build(); // Or return an error object
        }
    }

    //-------------------------------------------------------------
    // GET /api/vendor/orders/{orderId}
     @GetMapping("/{orderId}")
     public ResponseEntity<VendorOrderDetailsDto> getVendorOrderById(@PathVariable Long orderId) {
         Long vendorUserId = AuthUtil.getAuthenticatedUserId();
         Optional<Order> orderOptional = orderRepository.findOrderByOrderIdAndProductVendorId(orderId, vendorUserId);
         return orderOptional.map(VendorOrderDetailsDto::new)
                             .map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound().build());
     }


    @PostMapping("/place")
    public ResponseEntity<String> placeOrder(
            @RequestBody OrderRequest request) {  // Inject current logged-in user
        Long userId = AuthUtil.getAuthenticatedUserId(); // Get the authenticated user's ID

        orderService.placeOrder(request, userId);
        return ResponseEntity.ok("Order placed successfully");
    }
}