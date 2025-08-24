package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin", description = "API quản lý dành cho quản trị viên")
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

    @Operation(summary = "Lấy danh sách người dùng theo vai trò", 
            description = "Trả về danh sách người dùng theo vai trò được chỉ định. Nếu không có vai trò nào được chỉ định, trả về tất cả người dùng.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách người dùng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ khi lấy danh sách người dùng")
    })
    @GetMapping("/user/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<?>>> getAlUser(
            @Parameter(description = "Vai trò của người dùng (ADMIN, DRIVER, PASSENGER)") 
            @RequestParam(required = false) String role) {

        try {
            List<?> result = userService.getUsersByRole(role);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách người dùng thành công", HttpStatus.OK.value(), result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi lấy danh sách người dùng", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Xóa người dùng", 
            description = "Xóa người dùng khỏi hệ thống theo ID và gửi thông báo cho người dùng đó.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa người dùng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ khi xóa người dùng")
    })
    @DeleteMapping("/user/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(
            @Parameter(description = "ID của người dùng cần xóa") 
            @PathVariable Long id) {
        try {
            Optional<Users> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND.value(), null));
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

            return ResponseEntity.ok(new ApiResponse<>(true, "Xoá tài khoản thành công", HttpStatus.OK.value(), result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Xoá người dùng thất bại", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Lấy thông tin chi tiết của người dùng", 
            description = "Trả về thông tin chi tiết của người dùng theo ID.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin người dùng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể tìm thấy người dùng")
    })
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DriverDTO>> getUserDetail(
            @Parameter(description = "ID của người dùng cần xem thông tin") 
            @PathVariable Long id) {
        try {
            DriverDTO userDetails = userService.getUserDetails(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Tìm người dùng thành công", HttpStatus.OK.value(), userDetails));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Không thể tìm thấy người dùng", HttpStatus.BAD_REQUEST.value(), null));
        }
    }

    @Operation(summary = "Từ chối đăng ký tài xế", 
            description = "Từ chối đăng ký tài xế và gửi thông báo cho người dùng với lý do từ chối.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Từ chối tài xế thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Từ chối tài xế thất bại")
    })
    @PostMapping("/user/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> rejectUser(
            @Parameter(description = "ID của người dùng cần từ chối") 
            @PathVariable Long id, 
            @Parameter(description = "Lý do từ chối đăng ký tài xế") 
            @RequestParam String rejectionReason) {
        try {
            boolean success = userService.rejectUser(id, rejectionReason);

            userRepository.findById(id).ifPresent(user -> notificationService.sendNotification(
                    user.getEmail(),
                    "Đăng ký tài xế bị từ chối",
                    "Đăng ký tài xế của bạn đã bị từ chối. Lý do: " + rejectionReason,
                    "DRIVER_REJECTED",
                    id
            ));

            return ResponseEntity.ok(new ApiResponse<>(true, "Từ chối tài xế thành công", HttpStatus.OK.value(), success));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Từ chối tài xế thất bại", HttpStatus.BAD_REQUEST.value(), null));
        }
    }

    @Operation(summary = "Chấp nhận đăng ký tài xế", 
            description = "Chấp nhận đăng ký tài xế và gửi thông báo cho người dùng.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Chấp nhận tài xế thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Chấp nhận tài xế thất bại")
    })
    @PostMapping("/user/approved/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> approveUser(
            @Parameter(description = "ID của người dùng cần chấp nhận") 
            @PathVariable Long id) {
        try {
            boolean success = userService.approvedUser(id);

            userRepository.findById(id).ifPresent(user -> notificationService.sendNotification(
                    user.getEmail(),
                    "Đăng ký tài xế được chấp nhận",
                    "Đăng ký tài xế của bạn đã được chấp nhận. Bạn có thể bắt đầu tạo các chuyến đi.",
                    "DRIVER_APPROVED",
                    id
            ));

            return ResponseEntity.ok(new ApiResponse<>(true, "Chấp nhận tài xế thành công", HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Chấp nhận tài xế thất bại", HttpStatus.BAD_REQUEST.value()));
        }
    }
}
