package org.example.carpooling.Entity;

import jakarta.persistence.*;
import org.example.carpooling.Dto.ChatMessageDTO;


import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderEmail;
    private String receiverEmail;
    private String content;
    private String roomId;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;

    @Column(name = "is_read")
    private boolean isRead = false;

    // Enum for message status
    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ
    }

    public ChatMessage() {
        // Default constructor
    }

    public ChatMessage(ChatMessageDTO messageDTO) {
        this.senderEmail = messageDTO.getSenderEmail();
        this.receiverEmail = messageDTO.getReceiverEmail();
        this.content = messageDTO.getContent();
        this.roomId = messageDTO.getRoomId();
        this.timestamp = messageDTO.getTimestamp() != null ? messageDTO.getTimestamp() : LocalDateTime.now();
        this.isRead = messageDTO.isRead();
    }

    public ChatMessage(ChatMessage dto) {
        this.senderEmail = dto.getSenderEmail();
        this.receiverEmail = dto.getReceiverEmail();
        this.content = dto.getContent();
        this.roomId = dto.getRoomId();
        this.timestamp = dto.getTimestamp();
        this.status = dto.getStatus();
        this.isRead = dto.isRead();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
}
