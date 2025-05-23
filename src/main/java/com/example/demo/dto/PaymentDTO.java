package com.example.demo.dto;

import com.example.demo.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long paymentId;
    private Long orderId;
    private Long userId;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private String paymentMethod;
    private String referenceNo;
    private String transactionId;
    private PaymentStatus status;
    private int invoiceCount; // Instead of full invoice objects, just show the count
}
