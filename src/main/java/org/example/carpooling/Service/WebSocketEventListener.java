package org.example.carpooling.Service;

import org.example.carpooling.Entity.Users;
import org.example.carpooling.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    // Lưu trạng thái online của người dùng (email -> thời gian kết nối cuối)
    private static final Map<String, Long> userOnlineStatus = new ConcurrentHashMap<>();
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // Lấy thông tin người dùng từ principal
        if (event.getUser() instanceof UsernamePasswordAuthenticationToken) {
            String userEmail = ((UsernamePasswordAuthenticationToken) event.getUser()).getName();
            
            userOnlineStatus.put(userEmail, System.currentTimeMillis());
            
            // Gửi thông báo về trạng thái online
            broadcastUserStatus(userEmail, true);
            
            System.out.println("👤 Người dùng kết nối WebSocket: " + userEmail);
        }
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event.getUser() instanceof UsernamePasswordAuthenticationToken) {
            String userEmail = ((UsernamePasswordAuthenticationToken) event.getUser()).getName();
            
            userOnlineStatus.remove(userEmail);
            
            // Gửi thông báo về trạng thái offline
            broadcastUserStatus(userEmail, false);
            
            System.out.println("👤 Người dùng ngắt kết nối WebSocket: " + userEmail);
        }
    }
    
    private void broadcastUserStatus(String userEmail, boolean isOnline) {
        Optional<Users> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            Map<String, Object> statusUpdate = new ConcurrentHashMap<>();
            statusUpdate.put("userEmail", userEmail);
            statusUpdate.put("userName", user.getFullName());
            statusUpdate.put("online", isOnline);
            statusUpdate.put("timestamp", System.currentTimeMillis());
            
            // Broadcast tới tất cả người dùng
            messagingTemplate.convertAndSend("/topic/user-status", statusUpdate);
        }
    }
    
    // Phương thức kiểm tra người dùng có online không
    public static boolean isUserOnline(String userEmail) {
        return userOnlineStatus.containsKey(userEmail);
    }
    
    // Phương thức lấy thời gian hoạt động cuối cùng
    public static Long getLastActiveTime(String userEmail) {
        return userOnlineStatus.get(userEmail);
    }
}