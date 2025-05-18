package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderFilterRequest {

    // Filter by order status (e.g., "pending", "approved")
    private String status;

    // Filter by total amount (e.g., minimum total amount)
    private BigDecimal minTotalAmount;
    private BigDecimal maxTotalAmount;

    // Filter by product details within the order items
    private String productName;
    private String productCategory;

}