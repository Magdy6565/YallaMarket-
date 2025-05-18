package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderStatusUpdateRequest {

    @NotBlank(message = "Status cannot be empty")
    // Validate against the possible order_status enum values in your database
    @Pattern(regexp = "pending|approved|denied|shipped|delivered|cancelled", message = "Invalid order status") // Adjust regex based on your enum values
    private String status;

}