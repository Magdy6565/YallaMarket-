package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order; // One-to-one with Order

    @OneToOne
    @JoinColumn(name = "payment_id", unique = true)
    private Payment payment; // One-to-one with Payment

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(length = 50)
    private String status;

    @Column(name = "pdf_link")
    private String pdfLink;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}