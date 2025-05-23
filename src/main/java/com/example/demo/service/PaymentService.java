package com.example.demo.service;

import com.example.demo.dto.PaymentDTO;
import com.example.demo.model.Payment;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }    /**
     * Get all payments with optional filtering and pagination
     * 
     * @param paymentId Optional payment ID filter
     * @param orderId Optional order ID filter
     * @param userId Optional user ID filter
     * @param minAmount Optional minimum amount filter
     * @param maxAmount Optional maximum amount filter
     * @param status Optional payment status filter
     * @param page Page number (0-based)
     * @param size Page size
     * @return Page of filtered payments
     */
    public Page<PaymentDTO> getFilteredPayments(
            Long paymentId,
            Long orderId, 
            Long userId, 
            BigDecimal minAmount, 
            BigDecimal maxAmount, 
            PaymentStatus status, 
            int page, 
            int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        
        Specification<Payment> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Add filters if they are provided
            if (paymentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentId"), paymentId));
            }
            
            if (orderId != null) {
                predicates.add(criteriaBuilder.equal(root.get("orderId"), orderId));
            }
            
            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
            }
            
            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }
            
            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }
            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            // Only include non-deleted payments
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Payment> paymentPage = paymentRepository.findAll(spec, pageable);
        
        // Convert entities to DTOs
        List<PaymentDTO> paymentDTOs = paymentPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
            
        return new PageImpl<>(paymentDTOs, pageable, paymentPage.getTotalElements());
    }
    
    /**
     * Convert Payment entity to PaymentDTO
     */
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrderId());
        dto.setUserId(payment.getUserId());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setReferenceNo(payment.getReferenceNo());
        dto.setTransactionId(payment.getTransactionId());
        dto.setStatus(payment.getStatus());
        
        // Instead of full invoice objects, just store the count
        dto.setInvoiceCount(payment.getInvoices() != null ? payment.getInvoices().size() : 0);
        
        return dto;
    }    /**
     * Get a payment by ID
     * 
     * @param paymentId Payment ID
     * @return Optional containing the payment DTO if found
     */
    public Optional<PaymentDTO> getPaymentById(Long paymentId) {
        return paymentRepository.findByIdAndNotDeleted(paymentId)
            .map(this::convertToDTO);
    }
      /**
     * Get all payments for specific orders
     * 
     * @param orderIds List of order IDs
     * @return List of payment DTOs for those orders
     */
    public List<PaymentDTO> getPaymentsByOrderIds(List<Long> orderIds) {
        return paymentRepository.findAll((root, query, criteriaBuilder) -> 
            root.get("orderId").in(orderIds)
        ).stream()
         .map(this::convertToDTO)
         .collect(Collectors.toList());
    }
}
