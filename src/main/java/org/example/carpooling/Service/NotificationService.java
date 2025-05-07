package org.example.carpooling.Service;

import org.example.carpooling.Entity.Notification;
import java.util.List;

public interface NotificationService {
    
    // Gửi thông báo
    Notification sendNotification(String userEmail, String title, String content, String type, Long referenceId);
    
    // Lấy danh sách thông báo theo người dùng
    List<Notification> getNotifications(String userEmail);
    
    // Đánh dấu đã đọc
    void markAsRead(Long notificationId);
    
    // Đánh dấu tất cả đã đọc
    void markAllAsRead(String userEmail);
    
    // Đếm số thông báo chưa đọc
    long countUnreadNotifications(String userEmail);
    
    // Xóa thông báo cũ
    void deleteOldNotifications(int days);
}
