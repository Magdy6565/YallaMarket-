package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundRequestDto {
    @NotNull(message = "payment id cannot be null")
    private Long paymentId;
    @NotNull(message = "order item id cannot be null")
    private Long orderItemId;
    private String reason;
}
