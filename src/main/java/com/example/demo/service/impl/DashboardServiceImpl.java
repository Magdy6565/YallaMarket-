package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.enums.DeliveryStatus;
import com.example.demo.enums.PaymentStatus;
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
import java.util.List;
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
    public VendorDashboardResponse getVendorDashboard(Integer vendorId) {        // Get vendor's user ID (assuming it matches the vendorId for simplicity)
        Long userId = Long.valueOf(vendorId);
        
        // Count total products for vendor
        long totalProducts = productRepository.findByVendorIdAndDeletedAtIsNull(userId).size();
        
        // Count orders received
        long ordersReceived = orderRepository.countByUserId(userId);
        
        // Calculate total revenue
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenueForVendor(userId);
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
        long ordersPlaced = orderRepository.countByUserId(retailStoreId); // Changed to countByUserId and cast to Integer
        
        // Get delivery status breakdown
        List<OrderDeliveryStatusDto> deliveryStatusList = getDeliveryStatusBreakdown(retailStoreId.intValue()); // Changed to use intValue()
        
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
      private List<OrderDeliveryStatusDto> getDeliveryStatusBreakdown(Integer retailStoreId) { // Changed parameter to Integer
        List<OrderDeliveryStatusDto> result = new ArrayList<>();
        
        // Get all statuses
        DeliveryStatus[] allStatuses = DeliveryStatus.values();
        
        // For each status, count orders
        for (DeliveryStatus status : allStatuses) {
            long count = orderRepository.countByUserIdAndDeliveryStatus(retailStoreId, status); // Changed to countByUserIdAndDeliveryStatus
            result.add(new OrderDeliveryStatusDto(status.name(), count));
        }
        
        return result;
    }    private BigDecimal calculateTotalPaymentsMade(Long retailStoreId) {
        // Get all orders for the retail store
        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(retailStoreId); // Changed to findByUserIdOrderByOrderDateDesc and cast to Integer
        
        // Extract order IDs
        List<Long> orderIds = orders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());
        
        // Sum up payment amounts for these orders
        if (orderIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Use the repository directly to get just the payment amounts
        List<com.example.demo.model.Payment> payments = paymentRepository.findByStatusAndOrderIdIn(
                PaymentStatus.COMPLETED, orderIds);
        
        return payments.stream()
                .map(payment -> payment.getAmount())
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
            } catch (Exception e) {            // Ignore if product no longer exists
            }
            
            topProducts.add(new TopSellingProductDto(productId, productName, quantity));
        }
        
        return topProducts;
    }
}
 //{
  //"username": "admin_test",
  //"email": "admin_test@example.com",
  //"password": "adminpassword123",
