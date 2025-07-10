package org.example.carpooling.Service.Imp;

import org.example.carpooling.Entity.Notification;
import org.example.carpooling.Repository.NotificationRepository;
import org.example.carpooling.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImp implements NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Override
    public Notification sendNotification(String userEmail, String title, String content, String type, Long referenceId) {
        // 1. Tạo đối tượng thông báo
        Notification notification = new Notification();
        notification.setUserEmail(userEmail);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        // 2. Lưu thông báo vào database
        notification = notificationRepository.save(notification);
        
        // 3. Gửi thông báo qua WebSocket
        messagingTemplate.convertAndSend("/topic/notifications/" + userEmail, notification);
        
        return notification;
    }
    
    @Override
    public List<Notification> getNotifications(String userEmail) {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    }
    
    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }
    
    @Override
    public void markAllAsRead(String userEmail) {
        notificationRepository.markAllAsRead(userEmail);
    }
    
    @Override
    public long countUnreadNotifications(String userEmail) {
        return notificationRepository.countByUserEmailAndIsReadIsFalse(userEmail);
    }
    
    @Override
    public void deleteOldNotifications(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        notificationRepository.deleteByCreatedAtBefore(cutoffDate);
    }
} 