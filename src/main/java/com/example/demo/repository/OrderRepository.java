package com.example.demo.repository;

import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by vendor ID
    List<Order> findByVendorIdOrderByOrderDateDesc(Integer vendorId); // Changed CreatedAt to OrderDate
    
    // Find orders by retail store ID (now userId)
    List<Order> findByUserIdOrderByOrderDateDesc(Integer userId); // Changed field and OrderBy
    
    // Count orders by vendor ID
    long countByVendorId(Integer vendorId);
    
    // Count orders by retail store ID (now userId)
    long countByUserId(Integer userId);
    
    // Calculate total revenue for a vendor
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.vendorId = :vendorId AND o.paymentStatus = com.example.demo.model.PaymentStatus.COMPLETED") // Added fully qualified enum
    BigDecimal calculateTotalRevenueForVendor(@Param("vendorId") Integer vendorId);
    
    // Find orders by vendor ID and payment status
    List<Order> findByVendorIdAndPaymentStatus(Integer vendorId, com.example.demo.model.PaymentStatus paymentStatus);
    
    // Find orders by retail store ID (now userId) and payment status
    List<Order> findByUserIdAndPaymentStatus(Integer userId, com.example.demo.model.PaymentStatus paymentStatus);
      // Find orders by retail store ID (now userId) and delivery status
    List<Order> findByUserIdAndDeliveryStatus(Integer userId, com.example.demo.model.DeliveryStatus deliveryStatus);
    
    // Count orders by retail store ID (now userId) and delivery status
    long countByUserIdAndDeliveryStatus(Integer userId, com.example.demo.model.DeliveryStatus deliveryStatus);
}
