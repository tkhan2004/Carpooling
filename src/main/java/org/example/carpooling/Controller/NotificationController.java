package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Entity.Notification;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Lấy danh sách thông báo của người dùng đã đăng nhập
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            List<Notification> notifications = notificationService.getNotifications(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách thông báo", notifications));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Lỗi khi lấy danh sách thông báo", null));
        }
    }

    /**
     * Đánh dấu một thông báo đã đọc
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            notificationService.markAsRead(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã đánh dấu thông báo là đã đọc", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Lỗi khi đánh dấu đã đọc", null));
        }
    }

    /**
     * Đánh dấu tất cả thông báo đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            notificationService.markAllAsRead(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã đánh dấu tất cả thông báo là đã đọc", null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Lỗi khi đánh dấu tất cả là đã đọc", null));
        }
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnreadNotifications(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            long count = notificationService.countUnreadNotifications(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Số thông báo chưa đọc", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Lỗi khi đếm thông báo chưa đọc", null));
        }
    }
}