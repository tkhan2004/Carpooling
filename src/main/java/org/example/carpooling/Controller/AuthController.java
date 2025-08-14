package org.example.carpooling.Controller;

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


    @PostMapping(value = "/passenger-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserDTO>> passengerRegister(
            @RequestParam String email,
            @RequestParam @Valid String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestPart(value = "avatarImage", required = false) MultipartFile avatarImage) {

        try {
            email = email.trim().replaceAll(",$", "");
            fullName = fullName.trim().replaceAll(",$", "");
            phone = phone.trim().replaceAll(",$", "").replaceAll(" ", "");
            password = password.trim().replaceAll(",$", "").replaceAll(" ", "");


            Users registeredUser = userService.passengerRegister(email, password, fullName, phone, avatarImage);
            UserDTO userDTO = UserDTO.builder()
                    .email(registeredUser.getEmail())
                    .fullName(registeredUser.getFullName()).build();

            ApiResponse<UserDTO> successResponse = new ApiResponse<>(true, "Đăng ký thành công", userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (GlobalException ex) {
            ApiResponse<UserDTO> errorResponse = new ApiResponse<>(false, ex.getMessage(), null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<UserDTO> errorResponse = new ApiResponse<>(false, "Đăng ký thất bại: " + ex.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping(value = "/driver-register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DriverDTO>> driverRegister(
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password,
            @RequestParam("fullName") String fullName,
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("brand") String brand,
            @RequestParam("model") String model,
            @RequestParam("color") String color,
            @RequestParam("numberOfSeats") Integer numberOfSeats,
            @RequestPart(value = "avatarImage", required = false) MultipartFile avatarImage,
            @RequestPart(value = "licenseImage", required = false) MultipartFile licenseImage,
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

            ApiResponse<DriverDTO> successResponse = new ApiResponse<>(true, "Đăng ký thành công", driverDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(successResponse);

        } catch (GlobalException ex) {
            ApiResponse<DriverDTO> errorResponse = new ApiResponse<>(false, ex.getMessage(), null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<DriverDTO> errorResponse = new ApiResponse<>(false, "Đăng ký thất bại: " + ex.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
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
                        .body(new ApiResponse<>(false, "Người dùng không tồn tại", null));
            }

            Users user = optionalUser.get();
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());
            LoginResponse loginResponse = new LoginResponse(token, user.getEmail(), user.getRole().getName());
            ApiResponse<LoginResponse> successResponse = new ApiResponse<>(true, "Đăng nhập thành công", loginResponse);
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>(false, "Đăng nhập thất bại: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }


}
