package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "invoiceId"
)
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    @JsonIgnoreProperties({"invoice"})
    private Order order; // One-to-one with Order

    @ManyToOne
    @JoinColumn(name = "payment_id")
    @JsonIgnoreProperties("invoices")
    private Payment payment; // Many-to-one with Payment

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