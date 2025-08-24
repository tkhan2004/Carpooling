package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User", description = "API quản lý thông tin người dùng")
public class UserController
{
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Operation(summary = "Thay đổi mặt khẩu ",
            description = "Nhập mật khẩu cũ để thay đổi mật khẩu")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thay đổi mật khẩu thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Thay đổi mật khẩu thất bại")
    })
    @PutMapping("/change-pass")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER')")
    public ResponseEntity<?> changePass(@RequestBody ChangePassDTO  changePassDTO, HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String mgs = userService.changePass(token,  changePassDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, mgs, HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Đổi mật khẩu thất bại: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "Cập nhật thông tin cá nhân ",
            description = "Thay đổi thông tin cá nhân")

    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thay đổi thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Thay đổi thông tin thất bại")
    })
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

            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật hồ sơ thành công", HttpStatus.OK.value()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Cập nhật hồ sơ thất bại: " + e.getMessage(),HttpStatus.BAD_REQUEST.value()));
        }
    }


}
