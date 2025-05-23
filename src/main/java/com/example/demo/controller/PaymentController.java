package com.example.demo.controller;

import com.example.demo.dto.PaymentDTO;
import com.example.demo.service.PaymentService;
import com.example.demo.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Get payments with optional filtering
     * 
     * @param paymentId Optional payment ID filter
     * @param orderId Optional order ID filter
     * @param userId Optional user ID filter
     * @param minAmount Optional minimum amount filter
     * @param maxAmount Optional maximum amount filter
     * @param status Optional payment status filter
     * @param page Page number (0-based)
     * @param size Page size
     * @return Page of payments
     */    @GetMapping
    public ResponseEntity<Page<PaymentDTO>> getPayments(
            @RequestParam(required = false) Long paymentId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<PaymentDTO> payments = paymentService.getFilteredPayments(
                paymentId, orderId, userId, minAmount, maxAmount, status, page, size);
        
        return ResponseEntity.ok(payments);
    }

    /**
     * Get a single payment by ID
     * 
     * @param paymentId Payment ID
     * @return PaymentDTO if found, 404 otherwise
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long paymentId) {
        Optional<PaymentDTO> payment = paymentService.getPaymentById(paymentId);
        
        return payment
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
