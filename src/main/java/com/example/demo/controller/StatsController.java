// src/main/java/com/example/demo/controller/StatsController.java
package com.example.demo.controller;

import com.example.demo.dto.VendorStatsDTO;
import com.example.demo.service.VendorStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendor/stats")
public class StatsController {

    private final VendorStatsService vendorStatsService;

    public StatsController(VendorStatsService vendorStatsService) {
        this.vendorStatsService = vendorStatsService;
    }

    // Endpoint to get statistics for a specific vendor
    // In a real application, vendorId would often come from authenticated user context,
    // not directly as a path variable for security.
    @GetMapping("/{vendorId}")
    public ResponseEntity<VendorStatsDTO> getVendorStats(@PathVariable Long vendorId) {
        if (vendorId == null) {
            return ResponseEntity.badRequest().body(null); // Or throw an exception
        }
        VendorStatsDTO stats = vendorStatsService.getVendorStatistics(vendorId);
        return ResponseEntity.ok(stats);
    }
}