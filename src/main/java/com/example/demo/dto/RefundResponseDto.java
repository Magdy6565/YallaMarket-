package com.example.demo.dto;

import com.example.demo.model.Refund;
import com.example.demo.enums.RefundStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class RefundResponseDto {
    private Long refundId;
    private Long paymentId;
    private Long orderItemId;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private LocalDateTime refundDate;

    public RefundResponseDto(Refund refund) {
        this.refundId = refund.getRefundId();
        this.paymentId = refund.getPayment().getPaymentId();
        this.orderItemId = refund.getOrderItem().getOrderItemId();
        this.amount = refund.getAmount();
        this.reason = refund.getReason();
        this.status = refund.getStatus();
        this.refundDate = refund.getRefundDate();
    }
}
