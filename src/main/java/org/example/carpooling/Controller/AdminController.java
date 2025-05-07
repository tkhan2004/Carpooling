package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.CheckUserDTO;
import org.example.carpooling.Dto.DriverDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.NotificationService;
import org.example.carpooling.Service.RideService;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    RideService rideService;
    
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/user/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<?>>> getAlUser(@RequestParam(required = false) String role, HttpServletRequest request) {
        List<?> result = userService.getUsersByRole(role);

        ApiResponse<List<?>> response = new ApiResponse<>(
                true,
                "Lấy danh sách người dùng thành công",
                result
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        // Lấy thông tin người dùng trước khi xóa
        Optional<Users> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            Boolean result = userService.deleteUser(id);
            
            // Gửi thông báo về việc tài khoản bị xóa
            notificationService.sendNotification(
                user.getEmail(),
                "Tài khoản đã bị xóa",
                "Tài khoản của bạn đã bị quản trị viên xóa khỏi hệ thống.",
                "ACCOUNT_DELETED",
                id
            );
            
            ApiResponse<Boolean> response = new ApiResponse<>(true, "Xoá thành công", result);
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(false, "Không tìm thấy người dùng", null));
    }


    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DriverDTO>> getUserDetail(@PathVariable Long id) {
        DriverDTO userDetails = userService.getUserDetails(id);

        return ResponseEntity.ok(new ApiResponse<>(true, "User found", userDetails));
    }

    @PostMapping ("/user/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> rejectUser(@PathVariable Long id, @RequestParam String rejectionReason) {
        boolean success = userService.rejectUser((long) id, rejectionReason);
        
        // Lấy thông tin người dùng bị từ chối
        Optional<Users> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            
            // Gửi thông báo cho tài xế bị từ chối
            notificationService.sendNotification(
                user.getEmail(),
                "Đăng ký tài xế bị từ chối",
                "Đăng ký tài xế của bạn đã bị từ chối. Lý do: " + rejectionReason,
                "DRIVER_REJECTED",
                id
            );
        }
        
        ApiResponse<Boolean> response = new ApiResponse<>(true, "Từ chối tài xế thành công", success);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/approved/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> approveUser(@PathVariable Long id) {
        boolean success = userService.approvedUser((long) id);
        
        // Lấy thông tin người dùng được chấp nhận
        Optional<Users> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            
            // Gửi thông báo cho tài xế được chấp nhận
            notificationService.sendNotification(
                user.getEmail(),
                "Đăng ký tài xế được chấp nhận",
                "Đăng ký tài xế của bạn đã được chấp nhận. Bạn có thể bắt đầu tạo các chuyến đi.",
                "DRIVER_APPROVED",
                id
            );
        }
        
        ApiResponse<?> response = new ApiResponse<>(true, "Đã chấp nhận tài xế thành công", null);
        return ResponseEntity.ok(response);
    }
}

