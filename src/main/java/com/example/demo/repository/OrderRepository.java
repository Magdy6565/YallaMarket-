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
    List<Order> findByVendorIdOrderByCreatedAtDesc(Integer vendorId);
    
    // Find orders by retail store ID
    List<Order> findByRetailStoreIdOrderByCreatedAtDesc(Long retailStoreId);
    
    // Count orders by vendor ID
    long countByVendorId(Integer vendorId);
    
    // Count orders by retail store ID
    long countByRetailStoreId(Long retailStoreId);
    
    // Calculate total revenue for a vendor
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.vendorId = :vendorId AND o.paymentStatus = 'COMPLETED'")
    BigDecimal calculateTotalRevenueForVendor(@Param("vendorId") Integer vendorId);
    
    // Find orders by vendor ID and payment status
    List<Order> findByVendorIdAndPaymentStatus(Integer vendorId, com.example.demo.model.PaymentStatus paymentStatus);
    
    // Find orders by retail store ID and payment status
    List<Order> findByRetailStoreIdAndPaymentStatus(Long retailStoreId, com.example.demo.model.PaymentStatus paymentStatus);
      // Find orders by retail store ID and delivery status
    List<Order> findByRetailStoreIdAndDeliveryStatus(Long retailStoreId, com.example.demo.model.DeliveryStatus deliveryStatus);
    
    // Count orders by retail store ID and delivery status
    long countByRetailStoreIdAndDeliveryStatus(Long retailStoreId, com.example.demo.model.DeliveryStatus deliveryStatus);
}
