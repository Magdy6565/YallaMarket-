package com.example.demo.model;

import com.example.demo.enums.RefundStatus;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "refundId"
)
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    @JsonIgnoreProperties({"invoices"})
    private Payment payment;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;
}

