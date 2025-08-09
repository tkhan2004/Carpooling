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
import org.example.carpooling.Service.Imp.RideServiceImp;
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

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RideRepository rideRepository;


    @Autowired private NotificationService notificationService;

    @GetMapping("/user/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<?>>> getAlUser(@RequestParam(required = false) String role) {

        try {
            List<?> result = userService.getUsersByRole(role);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách người dùng thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi lấy danh sách người dùng", null));
        }
    }

    @DeleteMapping("/user/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable Long id) {
        try {
            Optional<Users> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Không tìm thấy người dùng", null));
            }

            Users user = userOpt.get();
            Boolean result = userService.deleteUser(id);

            notificationService.sendNotification(
                    user.getEmail(),
                    "Tài khoản đã bị xóa",
                    "Tài khoản của bạn đã bị quản trị viên xóa khỏi hệ thống.",
                    "ACCOUNT_DELETED",
                    id
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Xoá tài khoản thành công", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Xoá người dùng thất bại", null));
        }
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DriverDTO>> getUserDetail(@PathVariable Long id) {
        try {
            DriverDTO userDetails = userService.getUserDetails(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Tìm người dùng thành công", userDetails));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Không thể tìm thấy người dùng", null));
        }
    }

    @PostMapping("/user/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> rejectUser(@PathVariable Long id, @RequestParam String rejectionReason) {
        try {
            boolean success = userService.rejectUser(id, rejectionReason);

            userRepository.findById(id).ifPresent(user -> notificationService.sendNotification(
                    user.getEmail(),
                    "Đăng ký tài xế bị từ chối",
                    "Đăng ký tài xế của bạn đã bị từ chối. Lý do: " + rejectionReason,
                    "DRIVER_REJECTED",
                    id
            ));

            return ResponseEntity.ok(new ApiResponse<>(true, "Từ chối tài xế thành công", success));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Từ chối tài xế thất bại", null));
        }
    }

    @PostMapping("/user/approved/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> approveUser(@PathVariable Long id) {
        try {
            boolean success = userService.approvedUser(id);

            userRepository.findById(id).ifPresent(user -> notificationService.sendNotification(
                    user.getEmail(),
                    "Đăng ký tài xế được chấp nhận",
                    "Đăng ký tài xế của bạn đã được chấp nhận. Bạn có thể bắt đầu tạo các chuyến đi.",
                    "DRIVER_APPROVED",
                    id
            ));

            return ResponseEntity.ok(new ApiResponse<>(true, "Chấp nhận tài xế thành công", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Chấp nhận tài xế thất bại", null));
        }
    }
}

