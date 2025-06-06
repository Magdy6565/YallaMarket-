package com.example.demo.repository;

import com.example.demo.dto.VendorOrderDetailsDto;
import com.example.demo.enums.DeliveryStatus;
import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // hygeeb all orders for a specific user.
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.product p WHERE p.vendorId = :vendorId AND o.deletedAt IS NULL")
    List<Order> findOrdersByProductVendorId(@Param("vendorId") Long vendorId);

    //de htgeeb  specific order with order id x l user mo3yn
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.product p WHERE o.orderId = :orderId AND p.vendorId = :vendorId AND o.deletedAt IS NULL")
    Optional<Order> findOrderByOrderIdAndProductVendorId(@Param("orderId") Long orderId, @Param("vendorId") Long vendorId);

    // Leeh Upper ? 3l4an case sensitivvvve
    @Query("SELECT o FROM Order o WHERE UPPER(o.status) = UPPER(:orderStatusValue)")
    List<VendorOrderDetailsDto> findOrdersBySpecificStatus(@Param("orderStatusValue") String orderStatusValue);

    //Dool 3l4an el vendor statistics 3ayzeen no orders w sum el floos
    @Query("SELECT COUNT(DISTINCT o.id) FROM Order o JOIN o.orderItems oi JOIN oi.product p WHERE p.vendorId = :vendorId AND o.deletedAt IS NULL")
    Long countOrdersByProductVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.id IN " +
            "(SELECT DISTINCT oi.order.id FROM OrderItem oi JOIN oi.product p WHERE p.vendorId = :vendorId) " +
            "AND o.deletedAt IS NULL")
    Double sumRevenueByProductVendorId(@Param("vendorId") Long vendorId);

    List<Order> findByUserId(Long userId);

    // Find orders by retail store ID (now userId)
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId); // Changed field and OrderBy

    // Count orders by retail store ID (now userId)
    long countByUserId(Long userId);

    // Calculate total revenue for a vendor
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.userId = :userId AND o.paymentStatus = com.example.demo.enums.PaymentStatus.COMPLETED")
    // Added fully qualified enum
    BigDecimal calculateTotalRevenueForVendor(@Param("userId") Long userId);

    // Count orders by retail store ID (now userId) and delivery status
    long countByUserIdAndDeliveryStatus(Integer userId, DeliveryStatus deliveryStatus);

    // Find orders by user ID and status
    List<Order> findByUserIdAndStatus(Long userId, String status);
}