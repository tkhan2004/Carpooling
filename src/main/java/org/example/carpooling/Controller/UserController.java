package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.Request.ChangePassDTO;
import org.example.carpooling.Dto.Request.UserUpdateRequestDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/user")
public class UserController
{
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PutMapping("/change-pass")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER')")
    public ResponseEntity<?> changePass(@RequestBody ChangePassDTO  changePassDTO, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String mgs = userService.changePass(token,  changePassDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, mgs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Đổi mật khẩu thất bại: " + e.getMessage()));
        }
    }

    @PutMapping("/update-profile")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER')")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @RequestPart UserUpdateRequestDTO userUpdateRequestDTO,
            HttpServletRequest request) {

        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            Optional<Users> optionalUser = userService.findByEmail(email);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Không tìm thấy người dùng", null));
            }

            Users user = optionalUser.get();

            // Gọi service update
            userService.updateProfile(token,userUpdateRequestDTO);

            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật hồ sơ thành công"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Cập nhật hồ sơ thất bại: " + e.getMessage()));
        }
    }


}
