package org.example.carpooling.Controller;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.carpooling.Dto.*;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.FileService;
import org.example.carpooling.Service.Imp.FileServiceImp;
import org.example.carpooling.Service.Imp.UserServiceImp;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    FileService fileService;
    @Autowired
    private FileServiceImp fileServiceImp;

    @PostMapping(value = "/passenger-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserDTO>> passengerRegister(@RequestParam String email,
                                               @RequestParam String password,
                                               @RequestParam String fullName,
                                               @RequestParam String phone,
                                               @RequestPart(value = "avatarImage", required = false) MultipartFile avatarImage){
        email = email.trim().replaceAll(",$", "");  // Ví dụ: "khachhang5@gmail.com," -> "khachhang5@gmail.com"
        fullName = fullName.trim().replaceAll(",$", ""); // "Sakura," -> "Sakura"
        phone = phone.trim().replaceAll(",$", "").replaceAll(" ", ""); // "123457689 ," -> "123457689"
        password = password.trim().replaceAll(",$", "").replaceAll(" ", ""); // "123457689 ," -> "123457689"
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email.trim());
        request.setPassword(password.trim());
        request.setFullName(fullName.trim());
        request.setPhone(phone);
        try {
            Users registeredUser = userService.passengerRegister(request, avatarImage);
            UserDTO userDTO = new UserDTO(registeredUser, fileService);
            // Trường hợp thành công
            ApiResponse<UserDTO> successResponse = new ApiResponse<>(
                    true,
                    "Đăng ký thành công",
                    userDTO
            );
            return ResponseEntity.ok(successResponse);

        } catch (org.example.carpooling.Exception.Exception ex) {
            // Trường hợp email trùng
            ApiResponse<?> errorResponse = new ApiResponse<>(
                    false,
                    ex.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body((ApiResponse<UserDTO>) errorResponse);

        } catch (Exception ex) {
            // Trường hợp lỗi khác
            ApiResponse<?> errorResponse = new ApiResponse<>(
                    false,
                    "Đăng ký thất bại: " + ex.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((ApiResponse<UserDTO>) errorResponse);
        }
    }// Đăng ký

    @PostMapping(value = "/driver-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DriverDTO>> driverRegister(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestPart(value = "avatarImage", required = false) MultipartFile avatarImage,
            @RequestPart(value = "licenseImage", required = false) MultipartFile licenseImage,
            @RequestPart(value = "vehicleImage", required = false) MultipartFile vehicleImage) {

        // Xử lý trim() như cũ
        email = email.trim().replaceAll(",$", "");
        // ... (các xử lý trim khác)

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFullName(fullName);
        request.setPhone(phone);

        try {
            Users registeredUser = userService.driverRegister(request, avatarImage, licenseImage, vehicleImage);

            // Chuyển đổi sang DriverDTO
            DriverDTO driverDTO = new DriverDTO(
                    registeredUser.getId(),
                    registeredUser.getStatus(),
                    fileService.generateFileUrl(registeredUser.getLicenseImageUrl()),
                    fileService.generateFileUrl(registeredUser.getVehicleImageUrl()),
                    fileService.generateFileUrl(registeredUser.getAvatarImage()),
                    registeredUser.getFullName(),
                    registeredUser.getEmail(),
                    registeredUser.getPhone(),
                    registeredUser.getRole().getName()
            );

            ApiResponse<DriverDTO> successResponse = new ApiResponse<>(
                    true,
                    "Đăng ký thành công",
                    driverDTO
            );
            return ResponseEntity.ok(successResponse);

        } catch (org.example.carpooling.Exception.Exception ex) {
            // Xử lý lỗi như cũ
            ApiResponse<DriverDTO> errorResponse = new ApiResponse<>(
                    false,
                    ex.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<DriverDTO> errorResponse = new ApiResponse<>(
                    false,
                    "Đăng ký thất bại: " + ex.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        try {
            // 1. Xác thực email + password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // 2. Lấy user từ DB
            Optional<Users> optionalUser = userRepository.findByEmail(request.getEmail());
            if (!optionalUser.isPresent()) {
                ApiResponse<Users> successResponse = new ApiResponse<>(false,"Người dùng không tồn tại",null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(successResponse);
            }

            Users user = optionalUser.get();

            // 3. Sinh token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
            // 4. Trả response
            LoginResponse loginResponse = new LoginResponse(token,user.getEmail(),user.getRole().getName());
            ApiResponse<LoginResponse> successResponse = new ApiResponse<>(true,"Đang nhập thành công", loginResponse);
            return ResponseEntity.ok(successResponse);

        }

        catch (Exception e) {


//            byte[] key = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
//            String base64Key = Base64.getEncoder().encodeToString(key);
//            System.out.println("JWT Secret Key (Base64): " + base64Key);
//            => xin key
            ApiResponse<String> errorResponse = new ApiResponse<>(false, "Đăng nhập thất bại: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);        }
    }



}
