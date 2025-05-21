package com.example.demo.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity; // Nullable, will default to 0 in service

    private String category;
}