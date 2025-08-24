package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.ChatMessageDTO;
import org.example.carpooling.Dto.ChatMessagePayload;
import org.example.carpooling.Entity.ChatMessage;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.ChatMessageRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.ChatMessageService;
import org.example.carpooling.Service.RedisService.RedisChatPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
@Tag(name = "Chat", description = "API quản lý tin nhắn và phòng chat")
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisChatPublisher redisChatPublisher;

    @Operation(summary = "Xử lý tin nhắn WebSocket", 
            description = "Xử lý tin nhắn gửi qua WebSocket, lưu vào cơ sở dữ liệu và phát hành qua Redis")
    @MessageMapping("/chat/{roomId}")
    public ChatMessageDTO processMessage(
            @Parameter(description = "ID của phòng chat") 
            @DestinationVariable String roomId, 
            @Parameter(description = "Nội dung tin nhắn") 
            ChatMessageDTO messageDTO) {
        try {
            String token = messageDTO.getToken();
            if (token == null || token.isEmpty()) return null;

            String email = jwtUtil.extractUsername(token);
            if (email == null) return null;

            Optional<Users> senderOpt = userRepository.findByEmail(email);
            if (!senderOpt.isPresent()) return null;

            Users sender = senderOpt.get();

            // Gán thông tin đầy đủ vào DTO
            messageDTO.setSenderEmail(email);
            messageDTO.setSenderName(sender.getFullName());
            messageDTO.setTimestamp(LocalDateTime.now());
            messageDTO.setRoomId(roomId);
            messageDTO.setRead(false);

            // Lưu DB
            ChatMessage saved = chatMessageService.save(new ChatMessage(messageDTO));

            // Publish sang Redis (chỉ 1 lần ở đây thôi)
            ChatMessagePayload payload = new ChatMessagePayload(
                    saved.getRoomId(),
                    saved.getSenderEmail(),
                    saved.getReceiverEmail(),
                    saved.getContent(),
                    saved.getTimestamp(),
                    saved.isRead()
            );
            redisChatPublisher.publish(payload);

            return messageDTO;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Operation(summary = "Lấy tin nhắn của phòng chat", 
            description = "Trả về danh sách tin nhắn của một phòng chat cụ thể")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy tin nhắn thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập phòng chat"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/chat/{roomId}")
    public ResponseEntity<ApiResponse<List<ChatMessageDTO>>> getMessages(
            @Parameter(description = "ID của phòng chat") 
            @PathVariable String roomId, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            if (!roomId.contains(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>(false, "Không có quyền truy cập phòng chat này", HttpStatus.FORBIDDEN.value(), null));
            }

            chatMessageService.markMessagesAsRead(roomId, email);
            List<ChatMessageDTO> messages = chatMessageService.getMessageDTOsByRoomId(roomId);

            return ResponseEntity.ok(new ApiResponse<>(true, "Lịch sử tin nhắn", HttpStatus.OK.value(), messages));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi lấy lịch sử tin nhắn", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Lấy ID phòng chat", 
            description = "Tạo hoặc lấy ID phòng chat giữa người dùng hiện tại và một người dùng khác")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy ID phòng chat thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/chat/room/{otherUserEmail}")
    public ResponseEntity<ApiResponse<String>> getChatRoomId(
            @Parameter(description = "Email của người dùng khác") 
            @PathVariable String otherUserEmail, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            Optional<Users> otherUser = userRepository.findByEmail(otherUserEmail);
            if (!otherUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Người dùng không tồn tại", HttpStatus.NOT_FOUND.value(), null));
            }

            String roomId = chatMessageService.createRoomId(email, otherUserEmail);
            return ResponseEntity.ok(new ApiResponse<>(true, "ID phòng chat", HttpStatus.OK.value(), roomId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi lấy ID", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Đánh dấu tin nhắn đã đọc", 
            description = "Đánh dấu tất cả tin nhắn trong phòng chat là đã đọc")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đánh dấu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @PutMapping("/chat/{roomId}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @Parameter(description = "ID của phòng chat") 
            @PathVariable String roomId, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            chatMessageService.markMessagesAsRead(roomId, email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã đánh dấu tin nhắn là đã đọc", HttpStatus.OK.value(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Đã đánh dấu thất bại", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Gửi tin nhắn qua HTTP", 
            description = "Gửi tin nhắn qua HTTP thay vì WebSocket (chủ yếu dùng để test)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Gửi tin nhắn thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Không tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @PostMapping("/chat/test/{roomId}")
    public ResponseEntity<ApiResponse<ChatMessageDTO>> sendViaPostman(
            @Parameter(description = "ID của phòng chat") 
            @PathVariable String roomId, 
            @Parameter(description = "Nội dung tin nhắn") 
            @RequestBody ChatMessageDTO dto, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            Optional<Users> senderOpt = userRepository.findByEmail(email);
            if (!senderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Người dùng không tồn tại", HttpStatus.UNAUTHORIZED.value(), null));
            }

            dto.setSenderEmail(email);
            dto.setSenderName(senderOpt.get().getFullName());
            dto.setTimestamp(LocalDateTime.now());
            dto.setRoomId(roomId);

            chatMessageService.save(new ChatMessage(dto));
            redisChatPublisher.publish(new ChatMessagePayload(
                    dto.getRoomId(),
                    dto.getSenderEmail(),
                    dto.getReceiverEmail(),
                    dto.getContent(),
                    dto.getTimestamp(),
                    dto.isRead()
            ));
            return ResponseEntity.ok(new ApiResponse<>(true, "Tin nhắn đã được gửi", HttpStatus.OK.value(), dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Tin nhắn không được gửi", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Lấy danh sách phòng chat", 
            description = "Trả về danh sách các phòng chat của người dùng hiện tại")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/chat/rooms")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getChatRooms(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
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

            return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách phòng chat", HttpStatus.OK.value(), roomsList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lấy danh sách phòng chat thất bại", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

}
