package com.example.demo.repository;

import com.example.demo.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query(value = "SELECT oi.product_id, SUM(oi.quantity) as total_quantity " +
            "FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.order_id " +
            "WHERE o.store_id = :retailStoreId " +
            "GROUP BY oi.product_id " +
            "ORDER BY total_quantity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopSellingProductsForRetailStore(@Param("retailStoreId") Long retailStoreId, @Param("limit") int limit);
}