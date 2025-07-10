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
            String token = messageDTO.getToken();
            if (token == null || token.isEmpty()) return null;

            String email = jwtUtil.extractUsername(token);
            if (email == null) return null;

            Optional<Users> senderOpt = userRepository.findByEmail(email);
            if (!senderOpt.isPresent()) return null;

            Users sender = senderOpt.get();

            messageDTO.setSenderEmail(email);
            messageDTO.setSenderName(sender.getFullName());
            messageDTO.setTimestamp(LocalDateTime.now());
            messageDTO.setRoomId(roomId);
            messageDTO.setRead(false);

            ChatMessage entity = new ChatMessage(messageDTO);
            entity = chatMessageService.save(entity);

            simpMessagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getReceiverEmail(), messageDTO);
            simpMessagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getSenderEmail(), messageDTO);
            simpMessagingTemplate.convertAndSend("/topic/chat/" + roomId, messageDTO);

            return messageDTO;
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/chat/{roomId}")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getMessages(@PathVariable String roomId, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            if (!roomId.contains(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "Không có quyền truy cập phòng chat này", null));
            }

            chatMessageService.markMessagesAsRead(roomId, email);
            List<ChatMessageDTO> messages = chatMessageService.getMessageDTOsByRoomId(roomId);

            return ResponseEntity.ok(new ApiResponse<>(true, "Lịch sử tin nhắn", messages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi lấy lịch sử tin nhắn", null));
        }
    }

    @GetMapping("/chat/room/{otherUserEmail}")
    public ResponseEntity<ApiResponse<String>> getChatRoomId(@PathVariable String otherUserEmail, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            Optional<Users> otherUser = userRepository.findByEmail(otherUserEmail);
            if (!otherUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Người dùng không tồn tại", null));
            }

            String roomId = chatMessageService.createRoomId(email, otherUserEmail);
            return ResponseEntity.ok(new ApiResponse<>(true, "ID phòng chat", roomId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi lấy ID", null));
        }
    }

    @PutMapping("/chat/{roomId}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(@PathVariable String roomId, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            chatMessageService.markMessagesAsRead(roomId, email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã đánh dấu tin nhắn là đã đọc", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Đã đánh dấu thất bại", null));
        }
    }

    @PostMapping("/chat/test/{roomId}")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendViaPostman(@PathVariable String roomId, @RequestBody ChatMessageDTO dto, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            Optional<Users> senderOpt = userRepository.findByEmail(email);
            if (!senderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Người dùng không tồn tại", null));
            }

            dto.setSenderEmail(email);
            dto.setSenderName(senderOpt.get().getFullName());
            dto.setTimestamp(LocalDateTime.now());
            dto.setRoomId(roomId);

            chatMessageService.save(new ChatMessage(dto));
            simpMessagingTemplate.convertAndSend("/topic/chat/" + roomId, dto);

            return ResponseEntity.ok(new ApiResponse<>(true, "Tin nhắn đã được gửi", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Tin nhắn không được gửi", null));
        }
    }

    @GetMapping("/chat/rooms")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChatRooms(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            List<String> roomIds = chatMessageRepository.findRoomIdsByUserEmail(email);
            List<Map<String, Object>> roomsList = new ArrayList<>();

            for (String roomId : roomIds) {
                ChatMessage latestMessage = chatMessageRepository.findLatestMessageByRoomId(roomId);
                if (latestMessage == null) continue;

                String partnerEmail = latestMessage.getSenderEmail().equals(email)
                        ? latestMessage.getReceiverEmail()
                        : latestMessage.getSenderEmail();

                Optional<Users> partnerOpt = userRepository.findByEmail(partnerEmail);
                String partnerName = partnerOpt.map(Users::getFullName).orElse(partnerEmail);

                Map<String, Object> roomInfo = new HashMap<>();
                roomInfo.put("roomId", roomId);
                roomInfo.put("partnerEmail", partnerEmail);
                roomInfo.put("partnerName", partnerName);
                roomInfo.put("lastMessage", latestMessage.getContent());
                roomInfo.put("lastMessageTime", latestMessage.getTimestamp().toString());
                roomInfo.put("unreadCount", chatMessageRepository.countUnreadMessages(roomId, email));

                roomsList.add(roomInfo);
            }

            roomsList.sort((room1, room2) -> ((String) room2.get("lastMessageTime")).compareTo((String) room1.get("lastMessageTime")));

            return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách phòng chat", roomsList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lấy danh sách phòng chat thất bại", null));
        }
    }

}
