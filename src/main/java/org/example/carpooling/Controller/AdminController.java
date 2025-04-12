package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.CheckUserDTO;
import org.example.carpooling.Dto.DriverDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
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
    RideRepository rideRepository;

    @Autowired
    RideService rideService;

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
        Boolean users = userService.deleteUser(id);
        ApiResponse<Boolean> response = new ApiResponse<>(true, "Xoá thành công", users);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DriverDTO>> getUserDetail(@PathVariable Long id) {
        DriverDTO userDetails = userService.getUserDetails(id);

        return ResponseEntity.ok(new ApiResponse<>(true, "User found", userDetails));
    }

    @PostMapping ("/user/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> rejectUser(@PathVariable Long id, @RequestParam String rejectionReason ) {
        boolean success = userService.rejectUser((long) id, rejectionReason);
        ApiResponse<Boolean> response = new ApiResponse<>(true, "Từ chối tài xế thành công",success);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/approved/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> rejectUser(@PathVariable Long id) {
        boolean success = userService.approvedUser((long) id);
        ApiResponse<?> response = new ApiResponse<>(true, "Đã chấp nhận tài xế thành công",null);
        return ResponseEntity.ok(response);
    }



}

