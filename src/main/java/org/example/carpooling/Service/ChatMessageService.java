package org.example.carpooling.Service;

import org.example.carpooling.Dto.ChatMessageDTO;
import org.example.carpooling.Entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    // Lưu tin nhắn
    ChatMessage save(ChatMessage chatMessage);
    
    // Lấy tin nhắn theo phòng
    List<ChatMessage> getMessagesByRoomId(String roomId);
    
    // Lấy tin nhắn dạng DTO theo phòng
    List<ChatMessageDTO> getMessageDTOsByRoomId(String roomId);
    
    // Đánh dấu tin nhắn đã đọc
    void markMessagesAsRead(String roomId, String userEmail);
    
    // Tạo ID phòng từ email người dùng
    String createRoomId(String email1, String email2);
}
