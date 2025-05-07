package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Dto.UserDTO;
import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.BookingService;
import org.example.carpooling.Service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingService bookingService;
    
    @Autowired
    NotificationService notificationService;

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

    // Khách book chuyến đi
    @PostMapping("/booking/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> bookRides(@PathVariable Long rideId, @RequestParam int seats, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        
        // Đặt chuyến đi
        BookingDTO bookingDTO = bookingService.bookRide(rideId, seats, username);
        
        // Lấy thông tin booking
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingDTO.getId());
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // Gửi thông báo cho tài xế
            notificationService.sendNotification(
                booking.getRides().getDriver().getEmail(),
                "Có đặt chỗ mới",
                "Hành khách " + booking.getPassenger().getFullName() + " đã đặt " + seats + " chỗ cho chuyến đi từ " 
                    + booking.getRides().getDeparture() + " đến " + booking.getRides().getDestination(),
                "NEW_BOOKING",
                booking.getId()
            );
        }
        
        ApiResponse<BookingDTO> response = new ApiResponse<>(true, "Đăng ký chuyến đi thành công", bookingDTO);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    // Khách xác nhận thành công
    @PutMapping("/passenger-confirm/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> passengerConfirm(@PathVariable Long rideId, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        
        // Xác nhận hoàn thành
        bookingService.passengerConfirm(rideId, username);
        
        // Lấy booking để gửi thông báo
        Optional<Booking> bookingOpt = bookingRepository.findByRidesIdAndPassengerEmail(rideId, username);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // Gửi thông báo cho tài xế
            notificationService.sendNotification(
                booking.getRides().getDriver().getEmail(),
                "Hành khách đã xác nhận hoàn thành",
                "Hành khách " + booking.getPassenger().getFullName() + " đã xác nhận hoàn thành chuyến đi.",
                "PASSENGER_CONFIRMED",
                booking.getId()
            );
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Hành khách đã xác nhận hoàn thành", null));
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('PASSENGER')")
    // tái sử udngj driver booking
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getPassengerBookings(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String passengerEmail = jwtUtil.extractUsername(token);
        List<BookingDTO> bookings = bookingService.getBookingsForDriver(passengerEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách bookings của khách hàng", bookings));
    }

    @PutMapping("/cancel-bookings/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(@PathVariable Long rideId, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);
        
        // Lấy booking trước khi hủy
        Optional<Booking> bookingOpt = bookingRepository.findByRidesIdAndPassengerEmail(rideId, username);
        
        // Hủy booking
        BookingDTO bookingDTO = bookingService.cancleBookings(rideId, username);
        
        // Gửi thông báo cho tài xế nếu booking tồn tại
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            notificationService.sendNotification(
                booking.getRides().getDriver().getEmail(),
                "Hành khách đã hủy booking",
                "Hành khách " + booking.getPassenger().getFullName() + " đã hủy đặt chỗ cho chuyến đi từ " 
                    + booking.getRides().getDeparture() + " đến " + booking.getRides().getDestination(),
                "BOOKING_CANCELLED",
                booking.getId()
            );
        }
        
        ApiResponse<BookingDTO> response = new ApiResponse<>(true, "Huỷ chuyến đi thành công", bookingDTO);
        return ResponseEntity.ok(response);
    }
}
