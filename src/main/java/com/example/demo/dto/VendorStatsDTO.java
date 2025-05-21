// src/main/java/com/example/demo/dto/VendorStatsDTO.java
package com.example.demo.dto;

public class VendorStatsDTO {
    private Long totalProductsListed;
    private Long totalOrdersReceived;
    private Double totalRevenue;

    // Constructor
    public VendorStatsDTO(Long totalProductsListed, Long totalOrdersReceived, Double totalRevenue) {
        this.totalProductsListed = totalProductsListed;
        this.totalOrdersReceived = totalOrdersReceived;
        this.totalRevenue = totalRevenue;
    }

    // Getters (Setters are not strictly needed for DTOs returned from service)
    public Long getTotalProductsListed() {
        return totalProductsListed;
    }

    public Long getTotalOrdersReceived() {
        return totalOrdersReceived;
    }

    public Double getTotalRevenue() {
        // Ensure revenue is formatted to 2 decimal places if needed, or handle in frontend
        return totalRevenue != null ? Math.round(totalRevenue * 100.0) / 100.0 : 0.0;
    }

    // You can also add toString(), equals(), hashCode() if needed
}