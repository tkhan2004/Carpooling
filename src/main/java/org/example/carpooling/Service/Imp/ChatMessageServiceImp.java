package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.ChatMessageDTO;
import org.example.carpooling.Entity.ChatMessage;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Repository.ChatMessageRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatMessageServiceImp implements ChatMessageService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public ChatMessage save(ChatMessage chatMessage) {
        // Kiểm tra và đảm bảo dữ liệu cần thiết đã được cung cấp
        if (chatMessage.getSenderEmail() == null || chatMessage.getReceiverEmail() == null || 
            chatMessage.getContent() == null || chatMessage.getRoomId() == null) {
            throw new IllegalArgumentException("Missing required fields for chat message");
        }
        
        // Đảm bảo timestamp được thiết lập
        if (chatMessage.getTimestamp() == null) {
            chatMessage.setTimestamp(java.time.LocalDateTime.now());
        }
        
        return chatMessageRepository.save(chatMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessageDTOsByRoomId(String roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
        
        return messages.stream().map(message -> {
            ChatMessageDTO dto = new ChatMessageDTO(message);
            
            // Thêm tên người gửi
            Optional<Users> senderOpt = userRepository.findByEmail(message.getSenderEmail());
            if (senderOpt.isPresent()) {
                dto.setSenderName(senderOpt.get().getFullName());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void markMessagesAsRead(String roomId, String userEmail) {
        chatMessageRepository.markMessagesAsRead(roomId, userEmail);
    }
    
    @Override
    // Phương thức để tạo room_id một cách nhất quán
    public String createRoomId(String email1, String email2) {
        // Sắp xếp email để đảm bảo room_id luôn nhất quán
        String[] emails = {email1, email2};
        Arrays.sort(emails);
        return emails[0] + "_" + emails[1];
    }


}
