package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.ChatMessageDTO;
import org.example.carpooling.Entity.ChatMessage;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Repository.ChatMessageRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> getMessagesByRoomId(String roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }
    
    @Override
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
    public void markMessagesAsRead(String roomId, String userEmail) {
        chatMessageRepository.markMessagesAsRead(roomId, userEmail);
    }
    
    @Override
    public String createRoomId(String email1, String email2) {
        // Sắp xếp email để đảm bảo cùng một roomId cho cùng cặp người dùng
        String[] emails = {email1, email2};
        Arrays.sort(emails);
        return emails[0] + "_" + emails[1];
    }
}
