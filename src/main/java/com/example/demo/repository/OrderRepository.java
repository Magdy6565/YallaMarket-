package com.example.demo.repository;

import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Custom query to find Orders that contain products owned by a specific user (vendor)
    // This query joins Orders -> OrderItems -> Products and filters by product.vendor_id
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.product p WHERE p.vendorId = :vendorId AND o.deletedAt IS NULL")
    List<Order> findOrdersByProductVendorId(@Param("vendorId") Long vendorId);

    // Custom query to find a specific Order by ID that contains products owned by a specific user (vendor)
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi JOIN oi.product p WHERE o.orderId = :orderId AND p.vendorId = :vendorId AND o.deletedAt IS NULL")
    Optional<Order> findOrderByOrderIdAndProductVendorId(@Param("orderId") Long orderId, @Param("vendorId") Long vendorId);

    List<Order> findByUserId(Long userId);

    // You might need additional query methods for filtering later
}