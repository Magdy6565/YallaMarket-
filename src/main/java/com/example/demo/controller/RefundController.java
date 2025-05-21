package com.example.demo.controller;

import com.example.demo.dto.RefundRequestDto;
import com.example.demo.dto.RefundResponseDto;
import com.example.demo.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
