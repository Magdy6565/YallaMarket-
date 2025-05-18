package com.example.demo.repository;

import com.example.demo.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // You might add custom query methods here if needed,
    // but many operations can be done through the Order entity relationship.
    // For example, finding order items for a specific order:
    List<OrderItem> findByOrderId(Long orderId);
}