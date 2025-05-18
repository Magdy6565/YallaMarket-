package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetailStoreDashboardResponse {
    private long ordersPlaced;
    private List<OrderDeliveryStatusDto> deliveryStatus;
    private BigDecimal totalPaymentsMade;
    private List<TopSellingProductDto> topSellingProducts;
}
