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
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/update-profile")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER')")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @RequestPart("user") UserUpdateRequestDTO userDTO,
            @RequestPart(value = "avatarImageUrl", required = false) MultipartFile avatarImageUrl,
            @RequestPart(value = "licenseImageUrl", required = false) MultipartFile licenseImageUrl,
            @RequestPart(value = "vehicleImageUrl", required = false) MultipartFile vehicleImageUrl,
            HttpServletRequest request) {

        try {
            String token = jwtUtil.extractTokenFromRequest(request);

            String message = userService.updateProfile(
                    token,
                    userDTO,
                    avatarImageUrl,
                    licenseImageUrl,
                    vehicleImageUrl
            );

            return ResponseEntity.ok(
                    new ApiResponse<>(true, message, HttpStatus.OK.value())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Cập nhật hồ sơ thất bại: " + e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }


}
