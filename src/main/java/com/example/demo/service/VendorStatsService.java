// src/main/java/com/example/demo/service/VendorStatsService.java
package com.example.demo.service;

import com.example.demo.dto.VendorStatsDTO;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorStatsService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public VendorStatsService(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public VendorStatsDTO getVendorStatistics(Long vendorId) {
        // Total Products Listed
        Long totalProducts = productRepository.countByVendorId(vendorId);

        // Total Orders Received
        Long totalOrders = orderRepository.countOrdersByProductVendorId(vendorId);
        // Handle case where no orders, query returns null for COUNT
        if (totalOrders == null) {
            totalOrders = 0L;
        }

        // Total Revenue
        Double totalRevenue = orderRepository.sumRevenueByProductVendorId(vendorId);
        // Handle case where no revenue, query returns null for SUM
        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }

        return new VendorStatsDTO(totalProducts, totalOrders, totalRevenue);
    }
}