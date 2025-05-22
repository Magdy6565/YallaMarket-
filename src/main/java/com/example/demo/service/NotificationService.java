package com.example.demo.service;

import com.example.demo.dto.NotificationDto;
import com.example.demo.model.Notification;
import com.example.demo.model.NotificationType;

import java.util.List;

public interface NotificationService {
    
    /**
     * Create a new notification
     * 
     * @param userId User ID to send notification to
     * @param title Notification title
     * @param message Notification message
     * @param type Type of notification
     * @param referenceId Reference ID (e.g., order ID, product ID)
     * @return The created notification
     */
    Notification createNotification(Long userId, String title, String message, NotificationType type, Long referenceId);
    
    /**
     * Get recent notifications for a user
     * 
     * @param userId User ID
     * @param limit Maximum number of notifications to return
     * @return List of recent notifications
     */
    List<NotificationDto> getRecentNotificationsForUser(Long userId, int limit);
    
    /**
     * Mark a notification as read
     * 
     * @param notificationId Notification ID
     * @return true if successful, false otherwise
     */
    boolean markNotificationAsRead(Long notificationId);
    
    /**
     * Get count of unread notifications for a user
     * 
     * @param userId User ID
     * @return Count of unread notifications
     */
    long getUnreadNotificationCount(Long userId);
}
