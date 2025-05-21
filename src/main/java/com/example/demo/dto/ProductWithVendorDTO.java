package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductWithVendorDTO {
    private Long productId;
    private String name;
    private String productDescription;
    private BigDecimal price;
    private Integer quantity;
    private String category;

    private Long vendorId;
    private String vendorUsername;
    private String vendorEmail;
    private String vendorAddress;
    private BigDecimal vendorRating;
}
