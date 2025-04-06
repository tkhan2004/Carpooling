package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.CheckUserDTO;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Service.RideService;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    RideService rideService;

    @GetMapping("/user")
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


    @PostMapping("/user/reject/{id}")
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

