package com.example.demo.service;

import com.example.demo.dto.VendorDashboardResponse;
import java.math.BigDecimal;

public interface DashboardService {
    
    /**
     * Get dashboard data for a vendor
     * 
     * @param vendorId The ID of the vendor
     * @return Dashboard data for the vendor
     */
    VendorDashboardResponse getVendorDashboard(Integer vendorId);
    
    /**
     * Get dashboard data for a retail store
     * 
     * @param retailStoreId The ID of the retail store
     * @return Dashboard data for the retail store
     */
    com.example.demo.dto.RetailStoreDashboardResponse getRetailStoreDashboard(Long retailStoreId);
}
