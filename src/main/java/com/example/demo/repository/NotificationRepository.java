package com.example.demo.repository;

import com.example.demo.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find notifications by user ID
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find unread notifications by user ID
    List<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, boolean read);
    
    // Find notifications by user ID and type
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, com.example.demo.model.NotificationType type);
    
    // Count unread notifications by user ID
    long countByUserIdAndRead(Long userId, boolean read);
}
