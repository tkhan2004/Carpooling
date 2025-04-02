package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.ChangePassDTO;
import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Dto.UserUpdateDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Service.Imp.UserServiceImp;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
        String token = jwtUtil.extractTokenFromRequest(request);
        String mgs = userService.changePass(token,  changePassDTO);
        return ResponseEntity.ok(mgs);
    }

    @PutMapping("/update-profile")
    @PreAuthorize("hasAnyRole('DRIVER', 'PASSENGER')")
    public ResponseEntity<ApiResponse<?>> updateProfile(@RequestParam("fullName") String fullName,
                                                                           @RequestParam("phone") String phone,
                                                                           @RequestParam(value = "avatarImage", required = false) MultipartFile avatarImage,
                                                                           @RequestParam(value = "licenseImage", required = false) MultipartFile licenseImage,
                                                                           @RequestParam(value = "vehicleImage", required = false) MultipartFile vehicleImage,
                                                                           HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        Optional<Users> optionalUser = userService.findByEmail(email);

        if (optionalUser.isEmpty()) {
            ApiResponse<Object> response = (new ApiResponse<>(false,  "Không tìm thấy người dùng", null));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Users user = optionalUser.get();
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFullName(fullName);
        userUpdateDTO.setPhone(phone);

        if (user.getRole().getName().equals("DRIVER")) {
            userUpdateDTO.setAvatarImage(avatarImage);
            userUpdateDTO.setLicenseImageUrl(licenseImage);
            userUpdateDTO.setVehicleImageUrl(vehicleImage);
        }


        String message = userService.updateProfile(token, userUpdateDTO);
        ApiResponse<Object> response = (new ApiResponse<>(false,  message, null));
        return ResponseEntity.ok(response);
    }
}
