package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "Items list cannot be null")
    @NotEmpty(message = "Items list must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;
    private String paymentMethod;
}