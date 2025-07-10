package org.example.carpooling.Dto;

import org.example.carpooling.Entity.Notification;
import java.time.LocalDateTime;

public class NotificationDTO {
    private Long id;
    private String userEmail;
    private String title;
    private String content;
    private String type;
    private Long referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
    
    // Constructors
    public NotificationDTO() {
    }
    
    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.userEmail = notification.getUserEmail();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.type = notification.getType();
        this.referenceId = notification.getReferenceId();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
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