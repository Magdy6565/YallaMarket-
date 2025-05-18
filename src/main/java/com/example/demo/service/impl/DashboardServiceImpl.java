package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.model.DeliveryStatus;
import com.example.demo.model.Order;
import com.example.demo.model.Product;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.DashboardService;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    
    @Autowired
    public DashboardServiceImpl(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            NotificationService notificationService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.notificationService = notificationService;
    }
    
    @Override
    public VendorDashboardResponse getVendorDashboard(Integer vendorId) {
        // Get vendor's user ID (assuming it matches the vendorId for simplicity)
        Long userId = Long.valueOf(vendorId);
        
        // Count total products for vendor
        long totalProducts = productRepository.findByVendorIdAndDeletedAtIsNull(vendorId).size();
        
        // Count orders received
        long ordersReceived = orderRepository.countByVendorId(vendorId);
        
        // Calculate total revenue
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenueForVendor(vendorId);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }
        
        // Calculate total payments received
        BigDecimal totalPayments = paymentRepository.calculateTotalPaymentsForVendor(vendorId);
        if (totalPayments == null) {
            totalPayments = BigDecimal.ZERO;
        }
        
        // Get recent notifications
        List<NotificationDto> recentNotifications = notificationService.getRecentNotificationsForUser(userId, 5);
        
        return new VendorDashboardResponse(
                totalProducts,
                ordersReceived,
                totalRevenue,
                totalPayments,
                recentNotifications
        );
    }
    
    @Override
    public RetailStoreDashboardResponse getRetailStoreDashboard(Long retailStoreId) {
        // Count orders placed
        long ordersPlaced = orderRepository.countByRetailStoreId(retailStoreId);
        
        // Get delivery status breakdown
        List<OrderDeliveryStatusDto> deliveryStatusList = getDeliveryStatusBreakdown(retailStoreId);
        
        // Calculate total payments made
        BigDecimal totalPaymentsMade = calculateTotalPaymentsMade(retailStoreId);
        
        // Get top selling products
        List<TopSellingProductDto> topSellingProducts = getTopSellingProducts(retailStoreId, 5);
        
        return new RetailStoreDashboardResponse(
                ordersPlaced,
                deliveryStatusList,
                totalPaymentsMade,
                topSellingProducts
        );
    }
    
    private List<OrderDeliveryStatusDto> getDeliveryStatusBreakdown(Long retailStoreId) {
        List<OrderDeliveryStatusDto> result = new ArrayList<>();
        
        // Get all statuses
        DeliveryStatus[] allStatuses = DeliveryStatus.values();
        
        // For each status, count orders
        for (DeliveryStatus status : allStatuses) {
            long count = orderRepository.findByRetailStoreIdAndDeliveryStatus(retailStoreId, status).size();
            result.add(new OrderDeliveryStatusDto(status.name(), count));
        }
        
        return result;
    }
    
    private BigDecimal calculateTotalPaymentsMade(Long retailStoreId) {
        // Get all orders for the retail store
        List<Order> orders = orderRepository.findByRetailStoreIdOrderByCreatedAtDesc(retailStoreId);
        
        // Extract order IDs
        List<Long> orderIds = orders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());
        
        // Sum up payment amounts for these orders
        if (orderIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<com.example.demo.model.Payment> payments = paymentRepository.findByStatusAndOrderIdIn(
                com.example.demo.model.PaymentStatus.COMPLETED, orderIds);
        
        return payments.stream()
                .map(com.example.demo.model.Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private List<TopSellingProductDto> getTopSellingProducts(Long retailStoreId, int limit) {
        List<Object[]> results = orderItemRepository.findTopSellingProductsForRetailStore(retailStoreId, limit);
        List<TopSellingProductDto> topProducts = new ArrayList<>();
        
        for (Object[] result : results) {
            Long productId = ((Number) result[0]).longValue();
            int quantity = ((Number) result[1]).intValue();
            
            // Get product name
            String productName = "Unknown";
            try {
                Product product = productRepository.findById(productId).orElse(null);
                if (product != null) {
                    productName = product.getName();
                }
            } catch (Exception e) {
                // Ignore if product no longer exists
            }
            
            topProducts.add(new TopSellingProductDto(productId, productName, quantity));
        }
        
        return topProducts;
    }
}
