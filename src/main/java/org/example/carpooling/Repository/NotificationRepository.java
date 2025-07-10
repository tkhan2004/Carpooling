package org.example.carpooling.Repository;

import org.example.carpooling.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy thông báo theo email người dùng, sắp xếp theo thời gian tạo giảm dần (mới nhất lên đầu)
    List<Notification> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    // Đếm số thông báo chưa đọc
    long countByUserEmailAndIsReadIsFalse(String userEmail);
    
    // Đánh dấu thông báo đã đọc
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);
    
    // Đánh dấu tất cả thông báo của người dùng đã đọc
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userEmail = :userEmail AND n.isRead = false")
    void markAllAsRead(@Param("userEmail") String userEmail);
    
    // Xóa thông báo cũ hơn một ngày cụ thể
    @Modifying
    @Transactional
    void deleteByCreatedAtBefore(LocalDateTime date);
} 