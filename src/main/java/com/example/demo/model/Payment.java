package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order; // One-to-one with Order

    @Column(name = "user_id")
    private Long userId; // Foreign key to Users (no direct relationship shown here)

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(length = 50)
    private String status;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @OneToMany(mappedBy = "payment")
    private List<Invoice> invoices;

}