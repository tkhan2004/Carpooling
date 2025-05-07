package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.ChatMessageDTO;
import org.example.carpooling.Entity.ChatMessage;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * WebSocket endpoint để gửi tin nhắn
     */
    @MessageMapping("/chat/{roomId}")
    public void processMessage(@DestinationVariable String roomId, ChatMessageDTO messageDTO) {
        // Kiểm tra token
        String email = jwtUtil.extractUsername(messageDTO.getToken());
        if (email == null) return; // Token không hợp lệ

        // Lấy thông tin người gửi
        Optional<Users> senderOpt = userRepository.findByEmail(email);
        if (!senderOpt.isPresent()) return;
        
        Users sender = senderOpt.get();

        // Cập nhật thông tin tin nhắn
        messageDTO.setSenderEmail(email);
        messageDTO.setSenderName(sender.getFullName());
        messageDTO.setTimestamp(LocalDateTime.now());
        messageDTO.setRoomId(roomId);

        // Lưu tin nhắn vào DB
        ChatMessage entity = new ChatMessage(messageDTO);
        chatMessageService.save(entity);

        // Gửi tin nhắn đến tất cả client trong phòng
        simpMessagingTemplate.convertAndSend("/topic/chat/" + roomId, messageDTO);
    }

    /**
     * Lấy lịch sử tin nhắn của một phòng
     */
    @GetMapping("/chat/{roomId}")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getMessages(
            @PathVariable String roomId, 
            HttpServletRequest request) {
        
        // Xác thực người dùng
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        
        // Kiểm tra quyền truy cập phòng chat
        if (!roomId.contains(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Không có quyền truy cập phòng chat này", null));
        }
        
        // Đánh dấu tin nhắn đã đọc
        chatMessageService.markMessagesAsRead(roomId, email);
        
        // Lấy danh sách tin nhắn
        List<ChatMessageDTO> messages = chatMessageService.getMessageDTOsByRoomId(roomId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Lịch sử tin nhắn", messages));
    }
    
    /**
     * Tạo ID phòng chat giữa hai người dùng
     */
    @GetMapping("/chat/room/{otherUserEmail}")
    public ResponseEntity<ApiResponse<String>> getChatRoomId(
            @PathVariable String otherUserEmail,
            HttpServletRequest request) {
        
        // Xác thực người dùng
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        
        // Kiểm tra người dùng khác tồn tại
        Optional<Users> otherUser = userRepository.findByEmail(otherUserEmail);
        if (!otherUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Người dùng không tồn tại", null));
        }
        
        // Tạo ID phòng chat
        String roomId = chatMessageService.createRoomId(email, otherUserEmail);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "ID phòng chat", roomId));
    }
    
    /**
     * Đánh dấu tin nhắn đã đọc
     */
    @PutMapping("/chat/{roomId}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable String roomId,
            HttpServletRequest request) {
        
        // Xác thực người dùng
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        
        chatMessageService.markMessagesAsRead(roomId, email);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã đánh dấu tin nhắn là đã đọc", null));
    }

    /**
     * API Test để gửi tin nhắn qua Postman
     */
    @PostMapping("/chat/test/{roomId}")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendViaPostman(
            @PathVariable String roomId,
            @RequestBody ChatMessageDTO dto,
            HttpServletRequest request) {
        
        // Xác thực người dùng
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        
        // Lấy thông tin người gửi
        Optional<Users> senderOpt = userRepository.findByEmail(email);
        if (!senderOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "Người dùng không tồn tại", null));
        }
        
        // Cập nhật thông tin tin nhắn
        dto.setSenderEmail(email);
        dto.setSenderName(senderOpt.get().getFullName());
        dto.setTimestamp(LocalDateTime.now());
        dto.setRoomId(roomId);
        
        // Lưu và gửi tin nhắn
        chatMessageService.save(new ChatMessage(dto));
        simpMessagingTemplate.convertAndSend("/topic/chat/" + roomId, dto);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Tin nhắn đã được gửi", dto));
    }
}
