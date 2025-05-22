package com.example.demo.service.impl;

import com.example.demo.dto.NotificationDto;
import com.example.demo.model.Notification;
import com.example.demo.model.NotificationType;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    
    @Override
    public Notification createNotification(Long userId, String title, String message, NotificationType type, Long referenceId) {
        Notification notification = new Notification(userId, title, message, type, referenceId);
        return notificationRepository.save(notification);
    }
    
    @Override
    public List<NotificationDto> getRecentNotificationsForUser(Long userId, int limit) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        // Apply limit
        if (notifications.size() > limit) {
            notifications = notifications.subList(0, limit);
        }
        
        // Convert to DTOs
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean markNotificationAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.markAsRead();
            notificationRepository.save(notification);
            return true;
        }
        return false;
    }
    
    @Override
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }
    
    private NotificationDto convertToDto(Notification notification) {
        return new NotificationDto(
                notification.getNotificationId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType().name(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}
