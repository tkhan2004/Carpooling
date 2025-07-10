package org.example.carpooling.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String userEmail;    // Người nhận thông báo
    private String title;        // Tiêu đề thông báo
    private String content;      // Nội dung thông báo
    private String type;         // Loại thông báo (BOOKING, DRIVER_APPROVAL, RIDE_COMPLETED, ...)
    private Long referenceId;    // ID tham chiếu (booking_id, ride_id, ...)
    
    @Column(name = "is_read")
    private boolean isRead = false;        // Đã đọc chưa
    
    private LocalDateTime createdAt; // Thời gian tạo
    
    // Constructors
    public Notification() {
    }
    
    public Notification(String userEmail, String title, String content, String type, Long referenceId) {
        this.userEmail = userEmail;
        this.title = title;
        this.content = content;
        this.type = type;
        this.referenceId = referenceId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 