package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDashboardResponse {
    private long totalProducts;
    private long ordersReceived;
    private BigDecimal totalRevenue;
    private BigDecimal totalPayments;
    private List<NotificationDto> recentNotifications;
}
