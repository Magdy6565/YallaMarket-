package com.example.demo.controller;

import com.example.demo.dto.NotificationDto;
import com.example.demo.dto.RetailStoreDashboardResponse;
import com.example.demo.dto.VendorDashboardResponse;
import com.example.demo.model.UserRole;
import com.example.demo.service.DashboardService;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;
    private final NotificationService notificationService;
    
    @Autowired
    public DashboardController(DashboardService dashboardService, NotificationService notificationService) {
        this.dashboardService = dashboardService;
        this.notificationService = notificationService;
    }
    
    /**
     * Get vendor dashboard data
     * 
     * @param vendorId The ID of the vendor
     * @return Dashboard data for the vendor
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<VendorDashboardResponse> getVendorDashboard(@PathVariable Integer vendorId) {
        VendorDashboardResponse dashboardData = dashboardService.getVendorDashboard(vendorId);
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Get retail store dashboard data
     * 
     * @param retailStoreId The ID of the retail store
     * @return Dashboard data for the retail store
     */
    @GetMapping("/retail-store/{retailStoreId}")
    public ResponseEntity<RetailStoreDashboardResponse> getRetailStoreDashboard(@PathVariable Long retailStoreId) {
        RetailStoreDashboardResponse dashboardData = dashboardService.getRetailStoreDashboard(retailStoreId);
        return ResponseEntity.ok(dashboardData);
    }
    
    /**
     * Get recent notifications
     * 
     * @param userId The ID of the user
     * @param limit Maximum number of notifications to return (optional, default is 5)
     * @return List of recent notifications
     */
    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<NotificationDto>> getRecentNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        List<NotificationDto> notifications = notificationService.getRecentNotificationsForUser(userId, limit);
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Mark a notification as read
     * 
     * @param notificationId The ID of the notification
     * @return Success message or error
     */
    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long notificationId) {
        boolean success = notificationService.markNotificationAsRead(notificationId);
        if (success) {
            return ResponseEntity.ok("Notification marked as read");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
        }
    }
    
    /**
     * Get count of unread notifications
     * 
     * @param userId The ID of the user
     * @return Count of unread notifications
     */
    @GetMapping("/notifications/{userId}/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(count);
    }
}
