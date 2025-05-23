package com.example.demo.controller;

import com.example.demo.dto.RefundRequestDto;
import com.example.demo.dto.RefundResponseDto;
import com.example.demo.dto.RefundStatusUpdateDto;
import com.example.demo.enums.RefundStatus;
import com.example.demo.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    @Autowired
    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping
    public ResponseEntity<RefundResponseDto> createRefund(@RequestBody RefundRequestDto dto) {
        RefundResponseDto refund = refundService.createRefund(dto);
        return ResponseEntity.ok(refund);
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<RefundResponseDto>> getRefundsByVendor(@PathVariable Long vendorId) {
        List<RefundResponseDto> refunds = refundService.getRefundsByVendorId(vendorId);
        return ResponseEntity.ok(refunds);
    }
    
    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponseDto> getRefundById(@PathVariable Long refundId) {
        Optional<RefundResponseDto> refund = refundService.getRefundById(refundId);
        return refund.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PatchMapping("/{refundId}/status")
    public ResponseEntity<RefundResponseDto> updateRefundStatus(
            @PathVariable Long refundId, 
            @RequestBody RefundStatusUpdateDto statusUpdate) {
        try {
            // Parse status in lowercase (matches enum constants)
            RefundStatus newStatus = RefundStatus.valueOf(statusUpdate.getStatus().toLowerCase());
            Optional<RefundResponseDto> refund = refundService.updateRefundStatus(refundId, newStatus);
            return refund.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
