package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Dto.UserDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingRepository bookingReposioty;

    @Autowired
    BookingService bookingService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);

        String username = jwtUtil.extractUsername(token);
        Optional<Users> users =  userRepository.findByEmail(username);

        Users user = users.get();
        UserDTO userDTO = new UserDTO(user.getId(),user.getFullName(),user.getEmail(),user.getPhone(),user.getRole().getName());
        ApiResponse<UserDTO> response = new ApiResponse<>(true, "Thông tin người dùng",  userDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Khách book  nè
    @PostMapping("/booking/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> bookRides(@PathVariable Long rideId, @RequestParam int seats, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);

        BookingDTO bookingDTO = bookingService.bookRide(rideId, seats, username);
        ApiResponse<BookingDTO>  response = new ApiResponse<>(true, "Đăng ký chuyến đi thành công",  bookingDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // Khách xác nhận thành công
    @PutMapping("/passenger-confirm/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> passengerConfirm(@PathVariable Long rideId, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        bookingService.passengerConfirm(rideId, username);

        return ResponseEntity.ok(new ApiResponse<>(true, "Hành khách đã xác nhận hoàn thành", null));
    }

}
