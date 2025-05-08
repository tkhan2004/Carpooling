package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.ChatMessageDTO;
import org.example.carpooling.Entity.ChatMessage;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.ChatMessageRepository;
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
import java.util.*;

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

    @Autowired
    private ChatMessageRepository chatMessageRepository;


    @MessageMapping("/chat/{roomId}")
    public ChatMessageDTO processMessage(@DestinationVariable String roomId, ChatMessageDTO messageDTO) {
        try {
            System.out.println("===== WEBSOCKET DEBUG =====");
            System.out.println("Nhận tin nhắn từ roomId: " + roomId);
            System.out.println("Nội dung tin nhắn: " + messageDTO.getContent());
            System.out.println("Người nhận: " + messageDTO.getReceiverEmail());
            
            // Kiểm tra token
            String token = messageDTO.getToken();
            if (token == null || token.isEmpty()) {
                System.out.println("Token không tồn tại trong tin nhắn");
                return null;
            }
            
            System.out.println("Token từ client: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null"));
            
            String email = jwtUtil.extractUsername(token);
            if (email == null) {
                System.out.println("Token không hợp lệ khi gửi tin nhắn: " + token);
                return null; // Token không hợp lệ
            }
            System.out.println("Email từ token: " + email);

            // Lấy thông tin người gửi
            Optional<Users> senderOpt = userRepository.findByEmail(email);
            if (!senderOpt.isPresent()) {
                System.out.println("Không tìm thấy người dùng: " + email);
                return null;
            }

            Users sender = senderOpt.get();

            // Cập nhật thông tin tin nhắn
            messageDTO.setSenderEmail(email);
            messageDTO.setSenderName(sender.getFullName());
            messageDTO.setTimestamp(LocalDateTime.now());
            messageDTO.setRoomId(roomId);
            messageDTO.setRead(false); // Tin nhắn mới luôn chưa đọc

            // Lưu tin nhắn vào DB
            ChatMessage entity = new ChatMessage(messageDTO);
            entity = chatMessageService.save(entity);
            System.out.println("  Đã lưu tin nhắn vào DB với ID: " + entity.getId());

            // Gửi tin nhắn đến người nhận
            try {
                simpMessagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getReceiverEmail(), messageDTO);
                System.out.println("  Đã gửi tin nhắn đến người nhận: /topic/chat/" + messageDTO.getReceiverEmail());
            } catch (Exception e) {
                System.out.println("  Lỗi khi gửi tin nhắn đến người nhận: " + e.getMessage());
                e.printStackTrace();
            }

            // Gửi tin nhắn đến người gửi để cập nhật UI
            try {
                simpMessagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getSenderEmail(), messageDTO);
                System.out.println("  Đã gửi tin nhắn đến người gửi: /topic/chat/" + messageDTO.getSenderEmail());
            } catch (Exception e) {
                System.out.println("  Lỗi khi gửi tin nhắn đến người gửi: " + e.getMessage());
                e.printStackTrace();
            }

            // Gửi tin nhắn đến phòng chat chung
            try {
                simpMessagingTemplate.convertAndSend("/topic/chat/" + roomId, messageDTO);
                System.out.println("  Đã gửi tin nhắn đến phòng chat: /topic/chat/" + roomId);
            } catch (Exception e) {
                System.out.println("  Lỗi khi gửi tin nhắn đến phòng chat: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("Đã gửi tin nhắn WebSocket thành công");
            System.out.println("===== END WEBSOCKET DEBUG =====");
            return messageDTO;
        } catch (Exception e) {
            System.out.println("Lỗi khi xử lý tin nhắn WebSocket: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
    /**
     * Lấy danh sách phòng chat của người dùng hiện tại
     */
    @GetMapping("/chat/rooms")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChatRooms(HttpServletRequest request) {
        // Xác thực người dùng
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);

        // Lấy danh sách roomId của người dùng
        List<String> roomIds = chatMessageRepository.findRoomIdsByUserEmail(email);
        List<Map<String, Object>> roomsList = new ArrayList<>();

        for (String roomId : roomIds) {
            // Lấy tin nhắn mới nhất trong phòng
            ChatMessage latestMessage = chatMessageRepository.findLatestMessageByRoomId(roomId);
            if (latestMessage == null) continue;

            // Xác định email và tên đối tác
            String partnerEmail = latestMessage.getSenderEmail().equals(email)
                    ? latestMessage.getReceiverEmail()
                    : latestMessage.getSenderEmail();

            // Lấy thông tin người dùng đối tác
            Optional<Users> partnerOpt = userRepository.findByEmail(partnerEmail);
            String partnerName = partnerOpt.isPresent() ? partnerOpt.get().getFullName() : partnerEmail;

            // Tạo thông tin phòng chat
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("roomId", roomId);
            roomInfo.put("partnerEmail", partnerEmail);
            roomInfo.put("partnerName", partnerName);
            roomInfo.put("lastMessage", latestMessage.getContent());
            roomInfo.put("lastMessageTime", latestMessage.getTimestamp().toString());
            roomInfo.put("unreadCount", chatMessageRepository.countUnreadMessages(roomId, email));

            roomsList.add(roomInfo);
        }

        // Sắp xếp theo thời gian tin nhắn cuối cùng (mới nhất lên đầu)
        roomsList.sort((room1, room2) ->
                ((String)room2.get("lastMessageTime")).compareTo((String)room1.get("lastMessageTime")));

        return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách phòng chat", roomsList));
    }
}
