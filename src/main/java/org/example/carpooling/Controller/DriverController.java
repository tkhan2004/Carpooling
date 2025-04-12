package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Dto.UserDTO;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.BookingService;
import org.example.carpooling.Service.Imp.BookingServiceImp;
import org.example.carpooling.Service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    RideService rideService;

    @Autowired
    BookingRepository bookingReposioty;

    @Autowired
    BookingServiceImp bookingServiceImp;



    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('DRIVER')")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String username = jwtUtil.extractUsername(token);
        Optional<Users> users =  userRepository.findByEmail(username);
//        System.out.println("Extracted username from token: " + username);
//        System.out.println("Tìm thấy user: " + users.isPresent());
        if (!users.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        }

        Users user = users.get();
        UserDTO userDTO = new UserDTO(user.getId(),user.getFullName(),user.getEmail(),user.getPhone(),user.getRole().getName());
        return ResponseEntity.status(HttpStatus.OK).body(userDTO);
    }


    // coi chuyến đi
    @GetMapping("/my-rides")
    @PreAuthorize(("hasRole('DRIVER')"))
    public ResponseEntity<ApiResponse<List<RideRequestDTO>>> getRide(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);
        rideService.getRidesByDriverEmail(email);
        ApiResponse<List<RideRequestDTO>> response = new ApiResponse<>(true, "Danh sách chuyến đi", rideService.getRidesByDriverEmail(email));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/driver/bookings")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getDriverBookings(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String driverEmail = jwtUtil.extractUsername(token);
        List<BookingDTO> bookings = bookingServiceImp.getBookingsForDriver(driverEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách bookings của tài xế", bookings));
    }

    // Chấp nhận chuyến đi
    @PutMapping("/accept/{bookingId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverAcceptBooking(@PathVariable Long bookingId) {
        bookingServiceImp.driverAcceptBooking(bookingId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã chấp nhận hành khách", null));
    }


    // Xác nhận chuyến đi thành công
    @PutMapping("/complete/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverMarkCompleted(@PathVariable Long rideId) {
        bookingServiceImp.driverMarkCompleted(rideId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tài xế đã hoàn thành chuyến đi", null));
    }


}
