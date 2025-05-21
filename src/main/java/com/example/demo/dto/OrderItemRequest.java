package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "product id cannot be null")
    private Long productId;
    @NotNull(message = "quantity cannot be null")
    private Integer quantity;
}
