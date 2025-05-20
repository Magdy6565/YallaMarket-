package com.example.demo.service;

import com.example.demo.dto.OrderFilterRequest;
import com.example.demo.dto.VendorOrderDetailsDto;
import com.example.demo.model.Order;
import com.example.demo.model.OrderItem;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository; // Needed for filtering logic

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * Retrieves all non-deleted orders that contain products owned by the specified vendor (user).
     * @param vendorUserId The ID of the user who is the vendor.
     * @return A list of VendorOrderDetailsDto representing the orders.
     */
    @Transactional(readOnly = true) // Read-only transaction for fetching data
    public List<VendorOrderDetailsDto> getVendorOrders(Long vendorUserId) {
        List<Order> orders = orderRepository.findOrdersByProductVendorId(vendorUserId);

        // Map Order entities to VendorOrderDetailsDto
        return orders.stream()
                .map(VendorOrderDetailsDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a specific order, ensuring it contains products owned by the vendor.
     * @param orderId The ID of the order to update.
     * @param vendorUserId The ID of the user who is the vendor.
     * @param newStatus The new status for the order (e.g., "approved", "denied").
     * @return An Optional containing the updated Order if found and owned by the vendor, otherwise empty.
     */
    @Transactional
    public Optional<Order> updateOrderStatus(Long orderId, Long vendorUserId, String newStatus) {
        // Find the order and ensure it contains products from this vendor
        Optional<Order> orderOptional = orderRepository.findOrderByOrderIdAndProductVendorId(orderId, vendorUserId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            // TODO: Add validation here to ensure the newStatus is a valid transition from the current status
            order.setStatus(newStatus);
            return Optional.of(orderRepository.save(order));
        } else {
            return Optional.empty(); // Order not found or does not belong to this vendor
        }
    }

    public List<VendorOrderDetailsDto> getOrdersByStatus(OrderStatus status) {
        // Convert the OrderStatus enum to its String name (e.g., "PENDING")
        // and pass it to the new repository method.
        return orderRepository.findOrdersBySpecificStatus(status.name());
    }
}