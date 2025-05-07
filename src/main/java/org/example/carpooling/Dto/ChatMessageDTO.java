package org.example.carpooling.Dto;
import org.example.carpooling.Entity.ChatMessage;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private String token;           // token từ client
    private String senderEmail;     // email người gửi
    private String receiverEmail;   // email người nhận
    private String senderName;      // tên người gửi
    private String content;         // nội dung tin nhắn
    private String roomId;          // ID phòng chat
    private LocalDateTime timestamp; // thời gian gửi
    private boolean isRead = false;    // trạng thái đã đọc

    public ChatMessageDTO() {
    }
    
    public ChatMessageDTO(ChatMessage entity) {
        this.senderEmail = entity.getSenderEmail();
        this.receiverEmail = entity.getReceiverEmail();
        this.content = entity.getContent();
        this.roomId = entity.getRoomId();
        this.timestamp = entity.getTimestamp();
        this.isRead = entity.isRead();
    }

    // Getters và Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
