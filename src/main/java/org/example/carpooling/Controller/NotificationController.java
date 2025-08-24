package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Notifications", description = "API quản lý thông báo")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @Operation(summary = "Lấy danh sách thông báo", 
            description = "Trả về danh sách thông báo của người dùng đã đăng nhập")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            List<Notification> notifications = notificationService.getNotifications(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách thông báo", 200, notifications));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Lỗi khi lấy danh sách thông báo", 500, null));
        }
    }

    @Operation(summary = "Đánh dấu thông báo đã đọc", 
            description = "Đánh dấu một thông báo cụ thể là đã đọc")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đánh dấu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @Parameter(description = "ID của thông báo cần đánh dấu") 
            @PathVariable Long id, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            notificationService.markAsRead(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã đánh dấu thông báo là đã đọc", 200));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Lỗi khi đánh dấu đã đọc", 500));
        }
    }

    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc", 
            description = "Đánh dấu tất cả thông báo của người dùng là đã đọc")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đánh dấu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            notificationService.markAllAsRead(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã đánh dấu tất cả thông báo là đã đọc", 200));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Lỗi khi đánh dấu tất cả là đã đọc", 500));
        }
    }

    @Operation(summary = "Đếm số thông báo chưa đọc", 
            description = "Trả về số lượng thông báo chưa đọc của người dùng")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đếm thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnreadNotifications(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            long count = notificationService.countUnreadNotifications(email);
            return ResponseEntity.ok(new ApiResponse<>(true, "Số thông báo chưa đọc", 200, count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse<>(false, "Lỗi khi đếm thông báo chưa đọc", 500, null));
        }
    }
}
