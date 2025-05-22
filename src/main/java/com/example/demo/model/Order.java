package com.example.demo.model;

import com.example.demo.enums.DeliveryStatus;
import com.example.demo.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Setter
@Getter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Assuming SERIAL in DB
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // Foreign key to RetailStores

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate; // DATE type

    @Column(name = "status", nullable = false)
    private String status; // Maps to order_status enum in DB (e.g., 'pending', 'approved', 'denied')

    @Enumerated(EnumType.STRING) // Store enum as string
    @Column(name = "delivery_status") // Define the database column name
    private DeliveryStatus deliveryStatus; // New field for delivery status

    @Enumerated(EnumType.STRING) // Store enum as string
    @Column(name = "payment_status") // Define the database column name
    private PaymentStatus paymentStatus; // New field for payment status

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationship to OrderItems (One Order can have many OrderItems)
    // Use mappedBy to indicate the owning side is OrderItem
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

}
