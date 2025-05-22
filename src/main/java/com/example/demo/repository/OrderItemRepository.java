package com.example.demo.repository;

import com.example.demo.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // You might add custom query methods here if needed,
    // but many operations can be done through the Order entity relationship.
    // For example, finding order items for a specific order:
    List<OrderItem> findByOrderId(Long orderId);

    // Find order items by order ID
    List<OrderItem> findByOrderOrderId(Long orderId);

    // Find top-selling products for a vendor
    @Query(value = "SELECT oi.product_id, SUM(oi.quantity) as total_quantity " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "WHERE o.vendor_id = :vendorId " +
            "GROUP BY oi.product_id " +
            "ORDER BY total_quantity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopSellingProductsForVendor(@Param("vendorId") Integer vendorId, @Param("limit") int limit);      // Find top-selling products for a retail store

    @Query(value = "SELECT oi.product_id, SUM(oi.quantity) as total_quantity " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "WHERE o.store_id = :retailStoreId " +
            "GROUP BY oi.product_id " +
            "ORDER BY total_quantity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopSellingProductsForRetailStore(@Param("retailStoreId") Long retailStoreId, @Param("limit") int limit);
}