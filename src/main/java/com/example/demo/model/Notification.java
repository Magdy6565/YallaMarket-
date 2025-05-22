package com.example.demo.model;

import com.example.demo.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(name = "reference_id")
    private Long referenceId; // Can be order ID, product ID, etc.
    
    @Column(name = "is_read", nullable = false)
    private boolean read;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Default constructor
    public Notification() {
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructor with main fields
    public Notification(Long userId, String title, String message, NotificationType type, Long referenceId) {
        this();
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
    }
    
    // Mark notification as read
    public void markAsRead() {
        this.read = true;
    }
}
