package com.example.demo.dto;

import com.example.demo.model.OrderItem;
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
    private Long orderId;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private LocalDateTime refundDate;
    private String customerUsername;

    public RefundResponseDto(Refund refund) {
        this.refundId = refund.getRefundId();
        this.paymentId = refund.getPayment().getPaymentId();
        OrderItem orderItem = refund.getOrderItem();
        this.orderItemId = orderItem.getOrderItemId();
        this.orderId = orderItem.getOrder().getOrderId();
        this.amount = refund.getAmount();
        this.reason = refund.getReason();
        this.status = refund.getStatus();
        this.refundDate = refund.getRefundDate();
    }
}
