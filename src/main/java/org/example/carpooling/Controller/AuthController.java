package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.carpooling.Dto.*;
import org.example.carpooling.Dto.Request.LoginRequest;
import org.example.carpooling.Dto.Response.LoginResponse;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Entity.Vehicle;
import org.example.carpooling.Exception.GlobalException;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API xác thực và đăng ký người dùng")
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


    @Operation(summary = "Đăng ký tài khoản hành khách", 
            description = "Đăng ký tài khoản mới với vai trò hành khách")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping(value = "/passenger-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserDTO>> passengerRegister(
            @Parameter(description = "Email đăng nhập") 
            @RequestParam String email,
            @Parameter(description = "Mật khẩu") 
            @RequestParam @Valid String password,
            @Parameter(description = "Họ và tên") 
            @RequestParam String fullName,
            @Parameter(description = "Số điện thoại") 
            @RequestParam String phone,
            @Parameter(description = "Ảnh đại diện (tùy chọn)") 
            @RequestPart(value = "avatarImage", required = false) MultipartFile avatarImage) {

        try {
            email = email.trim().replaceAll(",$", "");
            fullName = fullName.trim().replaceAll(",$", "");
            phone = phone.trim().replaceAll(",$", "").replaceAll(" ", "");
            password = password.trim().replaceAll(",$", "").replaceAll(" ", "");


            Users registeredUser = userService.passengerRegister(email, phone, password, fullName, avatarImage);

            UserDTO userDTO = UserDTO.builder()
                    .id(registeredUser.getId())
                    .avatarUrl(registeredUser.getAvatarImage()) // giả sử entity có field này
                    .fullName(registeredUser.getFullName())
                    .email(registeredUser.getEmail())
                    .phoneNumber(registeredUser.getPhone())
                    .role(registeredUser.getRole().getName()) // nếu có bảng Role quan hệ
                    .build();

            ApiResponse<UserDTO> successResponse = new ApiResponse<>(true, "Đăng ký thành công", HttpStatus.CREATED.value(), userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (GlobalException ex) {
            ApiResponse<UserDTO> errorResponse = new ApiResponse<>(false, ex.getMessage(), HttpStatus.CONFLICT.value(), null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<UserDTO> errorResponse = new ApiResponse<>(false, "Đăng ký thất bại: " + ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "Đăng ký tài khoản tài xế", 
            description = "Đăng ký tài khoản mới với vai trò tài xế, bao gồm thông tin xe")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Đăng ký thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email đã tồn tại"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping(value = "/driver-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DriverDTO>> driverRegister(
            @Parameter(description = "Email đăng nhập") 
            @RequestParam("email") String email,
            @Parameter(description = "Số điện thoại") 
            @RequestParam("phone") String phone,
            @Parameter(description = "Mật khẩu") 
            @RequestParam("password") String password,
            @Parameter(description = "Họ và tên") 
            @RequestParam("fullName") String fullName,
            @Parameter(description = "Biển số xe") 
            @RequestParam("licensePlate") String licensePlate,
            @Parameter(description = "Hãng xe") 
            @RequestParam("brand") String brand,
            @Parameter(description = "Mẫu xe") 
            @RequestParam("model") String model,
            @Parameter(description = "Màu xe") 
            @RequestParam("color") String color,
            @Parameter(description = "Số ghế") 
            @RequestParam("numberOfSeats") Integer numberOfSeats,
            @Parameter(description = "Ảnh đại diện (tùy chọn)") 
            @RequestPart(value = "avatarImage", required = false) MultipartFile avatarImage,
            @Parameter(description = "Ảnh giấy phép lái xe (tùy chọn)") 
            @RequestPart(value = "licenseImage", required = false) MultipartFile licenseImage,
            @Parameter(description = "Ảnh xe (tùy chọn)") 
            @RequestPart(value = "vehicleImage", required = false) MultipartFile vehicleImage
    ) {
        try {
            // Gọi service đăng ký
            Users registeredUser = userService.driverRegister(
                    email, phone, password, fullName,
                    licensePlate, brand, model, color, numberOfSeats,
                    avatarImage, licenseImage, vehicleImage
            );
            Vehicle vehicle = null;
            if ( registeredUser.getVehicles() != null && !registeredUser.getVehicles().isEmpty()){
                vehicle = registeredUser.getVehicles().get(0);
            }

           DriverDTO driverDTO =DriverDTO.builder()
                                        .id(registeredUser.getId())
                                        .status(registeredUser.getStatus())
                                        .avatarImage(registeredUser.getAvatarImage())
                                        .fullName(registeredUser.getFullName())
                                        .email(registeredUser.getEmail())
                                        .phoneNumber(registeredUser.getPhone())
                                        .licensePlate(vehicle != null ? vehicle.getLicensePlate() : null)
                                        .brand(vehicle != null ? vehicle.getBrand() : null)
                                        .model(vehicle != null ? vehicle.getModel() : null)
                                        .color(vehicle != null ? vehicle.getColor() : null)
                                        .numberOfSeats(vehicle != null ? vehicle.getNumberOfSeats() : null)
                                        .vehicleImageUrl(vehicle != null ? vehicle.getVehicleImageUrl() : null)
                                        .licenseImageUrl(vehicle != null ? vehicle.getLicenseImageUrl() : null)
                                        .build();

            ApiResponse<DriverDTO> successResponse = new ApiResponse<>(true, "Đăng ký thành công", HttpStatus.CREATED.value(), driverDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (GlobalException ex) {
            ApiResponse<DriverDTO> errorResponse = new ApiResponse<>(false, ex.getMessage(), HttpStatus.CONFLICT.value(), null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<DriverDTO> errorResponse = new ApiResponse<>(false, "Đăng ký thất bại: " + ex.getMessage(), HttpStatus.BAD_REQUEST.value(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "Đăng nhập", 
            description = "Xác thực người dùng và trả về token JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đăng nhập thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Đăng nhập thất bại")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Parameter(description = "Thông tin đăng nhập") 
            @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            Optional<Users> optionalUser = userRepository.findByEmail(request.getEmail());
            if (!optionalUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Người dùng không tồn tại", HttpStatus.UNAUTHORIZED.value(), null));
            }

            Users user = optionalUser.get();
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
            LoginResponse loginResponse = new LoginResponse(token, user.getEmail(), user.getRole().getName());
            ApiResponse<LoginResponse> successResponse = new ApiResponse<>(true, "Đăng nhập thành công", HttpStatus.OK.value(), loginResponse);
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>(false, "Đăng nhập thất bại: " + e.getMessage(), HttpStatus.UNAUTHORIZED.value(), null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }


}
