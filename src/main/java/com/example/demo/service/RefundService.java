package com.example.demo.service;

import com.example.demo.dto.RefundRequestDto;
import com.example.demo.dto.RefundResponseDto;
import com.example.demo.model.OrderItem;
import com.example.demo.model.Payment;
import com.example.demo.model.Refund;
import com.example.demo.enums.RefundStatus;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;    public RefundResponseDto createRefund(RefundRequestDto dto) {
        Payment payment = paymentRepository.findById(dto.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        OrderItem orderItem = orderItemRepository.findById(dto.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order Item not found"));

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setOrderItem(orderItem);
        refund.setAmount(orderItem.getPriceEach().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        refund.setReason(dto.getReason());
        refund.setRefundDate(LocalDateTime.now());
        refund.setStatus(RefundStatus.requested);
        
        // Mark the order item as deleted (soft delete)
        orderItem.setDeletedAt(LocalDateTime.now());
        orderItemRepository.save(orderItem);

        refund = refundRepository.save(refund);

        return new RefundResponseDto(refund);
    }

    public List<RefundResponseDto> getRefundsByVendorId(Long vendorId) {
        List<Refund> refunds = refundRepository.findRefundsByVendorId(vendorId);
        return refunds.stream()
                .map(RefundResponseDto::new)
                .collect(Collectors.toList());
    }
}
