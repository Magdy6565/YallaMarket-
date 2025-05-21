package com.example.demo.controller;

import com.example.demo.dto.VendorOrderDetailsDto;
import com.example.demo.service.OrderService;
import com.example.demo.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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


    @GetMapping
    public ResponseEntity<List<VendorOrderDetailsDto>> getAllStoreOrders() {
        Long userId = AuthUtil.getAuthenticatedUserId(); // Get the authenticated user's ID
        List<VendorOrderDetailsDto> orders = orderService.getAllStoreOrdersInvoicesByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}