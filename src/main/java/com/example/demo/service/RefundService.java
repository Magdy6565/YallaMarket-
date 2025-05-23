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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final com.example.demo.repository.UserRepository userRepository;
    private final JavaMailSender mailSender;
    
    public RefundResponseDto createRefund(RefundRequestDto dto) {
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
        
        refund = refundRepository.save(refund);

        return new RefundResponseDto(refund);
    }

    public List<RefundResponseDto> getRefundsByVendorId(Long vendorId) {
        List<Refund> refunds = refundRepository.findRefundsByVendorId(vendorId);
        List<RefundResponseDto> dtos = new ArrayList<>();
        for (Refund refund : refunds) {
            RefundResponseDto dto = new RefundResponseDto(refund);
            // Fetch retail store (customer) username
            Long storeId = refund.getOrderItem().getOrder().getUserId();
            userRepository.findById(storeId)
                .ifPresent(user -> dto.setCustomerUsername(user.getUsername()));
            dtos.add(dto);
        }
        return dtos;
    }
    
    public Optional<RefundResponseDto> getRefundById(Long refundId) {
        return refundRepository.findById(refundId)
                .map(RefundResponseDto::new);
    }
    
    @Transactional
    public Optional<RefundResponseDto> updateRefundStatus(Long refundId, RefundStatus newStatus) {
        return refundRepository.findById(refundId)
            .map(refund -> {
                refund.setStatus(newStatus);
                Refund updated = refundRepository.save(refund);
                // Notify retail store via email
                Long storeId = updated.getOrderItem().getOrder().getUserId();
                userRepository.findById(storeId).ifPresent(storeUser -> {
                    String to = storeUser.getEmail();
                    String subject = "Your refund request #" + updated.getRefundId() + " has been " + newStatus.name().toLowerCase();
                    String text = "Hello " + storeUser.getUsername() + ",\n\n" +
                        "Your refund request for order #" + updated.getOrderItem().getOrder().getOrderId() +
                        " has been " + newStatus.name().toLowerCase() + ".\n" +
                        "Amount: $" + updated.getAmount() + "\n" +
                        "Reason: " + updated.getReason() + "\n\n" +
                        "Thank you,\nYallaMarket Team";
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(to);
                    message.setSubject(subject);
                    message.setText(text);
                    mailSender.send(message);
                });
                return new RefundResponseDto(updated);
            });
    }
}
