package com.example.demo.controller;

import com.example.demo.dto.VendorOrderDetailsDto;
import com.example.demo.model.Invoice;
import com.example.demo.model.Order;
import com.example.demo.repository.InvoiceRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderService;
import com.example.demo.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/store/orders") // Base path for store order management
public class OrderStoreController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;@Autowired
    public OrderStoreController(OrderService orderService, OrderRepository orderRepository, InvoiceRepository invoiceRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.invoiceRepository = invoiceRepository;
    }


    @GetMapping
    public ResponseEntity<List<VendorOrderDetailsDto>> getAllStoreOrders() {
        Long userId = AuthUtil.getAuthenticatedUserId(); // Get the authenticated user's ID
        List<VendorOrderDetailsDto> orders = orderService.getAllStoreOrdersInvoicesByUserId(userId);
        return ResponseEntity.ok(orders);
    }
      @GetMapping("/{orderId}")
    public ResponseEntity<VendorOrderDetailsDto> getOrderById(@PathVariable Long orderId) {
        // Retrieve the order by ID without authentication check
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        
        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            Invoice invoice = invoiceRepository.findByOrder(order);
            return ResponseEntity.ok(new VendorOrderDetailsDto(order, invoice));
        }
        
        return ResponseEntity.notFound().build();
    }
      // Method to handle order cancellation
    @org.springframework.web.bind.annotation.PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        try {
            Optional<Order> orderOptional = orderRepository.findById(orderId);
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                order.setStatus(com.example.demo.enums.OrderStatus.CANCELLED.name());
                orderRepository.save(order);
                return ResponseEntity.ok("Order cancelled successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to cancel order: " + e.getMessage());
        }
    }
      @GetMapping("/filter")
    public ResponseEntity<List<VendorOrderDetailsDto>> getFilteredStoreOrders(
            @RequestParam(required = false) String status) {
        try {
            // If status is null or empty, return all orders
            if (status == null || status.isEmpty()) {
                return getAllStoreOrders();
            }
            
            // Convert the string status to enum if needed
            com.example.demo.enums.OrderStatus orderStatus = com.example.demo.enums.OrderStatus.valueOf(status);
            
            // Get authenticated user ID
            Long userId;
            try {
                userId = AuthUtil.getAuthenticatedUserId();
            } catch (Exception e) {
                // If authentication fails, use a fallback approach
                return ResponseEntity.ok(orderService.getOrdersByStatus(orderStatus));
            }
            
            // Get orders filtered by status and user ID
            List<VendorOrderDetailsDto> filteredOrders = orderService.getOrdersByStatusForUser(orderStatus, userId);
            return ResponseEntity.ok(filteredOrders);
        } catch (IllegalArgumentException e) {
            // Invalid status provided
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}