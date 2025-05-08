package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Dto.DriverDTO;
import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Dto.UserDTO;
import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.BookingService;
import org.example.carpooling.Service.Imp.BookingServiceImp;
import org.example.carpooling.Service.NotificationService;
import org.example.carpooling.Service.RideService;
import org.example.carpooling.Service.UserService;
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
    BookingRepository bookingRepository;

    @Autowired
    BookingService bookingService;
    
    @Autowired
    NotificationService notificationService;

    @Autowired
    UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('DRIVER')")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String email = jwtUtil.extractUsername(token);
        Optional<Users> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
        }

        Users user = optionalUser.get();
        DriverDTO driverDTO = userService.getUserDetails(user.getId());

        return ResponseEntity.ok(driverDTO);

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

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getDriverBookings(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String driverEmail = jwtUtil.extractUsername(token);
        List<BookingDTO> bookings = bookingService.getBookingsForDriver(driverEmail);
        return ResponseEntity.ok(new ApiResponse<>(true, "Danh sách bookings của tài xế", bookings));
    }

    // Chấp nhận chuyến đi
    @PutMapping("/accept/{bookingId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverAcceptBooking(@PathVariable Long bookingId) {
        // Lấy thông tin booking trước khi xử lý
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            
            // Chấp nhận booking
            bookingService.driverAcceptBooking(bookingId);
            
            // Gửi thông báo cho hành khách
            notificationService.sendNotification(
                booking.getPassenger().getEmail(),
                "Tài xế đã chấp nhận chuyến đi",
                "Tài xế " + booking.getRides().getDriver().getFullName() + " đã chấp nhận chuyến đi của bạn từ " 
                    + booking.getRides().getDeparture() + " đến " + booking.getRides().getDestination(),
                "BOOKING_ACCEPTED",
                bookingId
            );
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã chấp nhận hành khách", null));
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(false, "Không tìm thấy booking", null));
    }

    @PutMapping("/reject/{bookingId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverRejectBooking(@PathVariable Long bookingId) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Không tìm thấy booking", null));
        }

        Booking booking = bookingOpt.get();

        // Cập nhật trạng thái booking
        booking.setStatus(BookingStatus.REJECTED);
        bookingRepository.save(booking);

        // Gửi thông báo cho hành khách
        notificationService.sendNotification(
                booking.getPassenger().getEmail(),
                "Tài xế đã từ chối chuyến đi",
                "Rất tiếc, tài xế " + booking.getRides().getDriver().getFullName() +
                        " đã từ chối chuyến đi từ " + booking.getRides().getDeparture() +
                        " đến " + booking.getRides().getDestination(),
                "BOOKING_REJECTED",
                booking.getId()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Đã từ chối hành khách", null));
    }

    // Xác nhận chuyến đi thành công
    @PutMapping("/complete/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverMarkCompleted(@PathVariable Long rideId) {
        // Lấy thông tin các booking của chuyến đi
        List<Booking> bookings = bookingRepository.findByRidesId(rideId);
        
        // Đánh dấu chuyến đi đã hoàn thành
        bookingService.driverMarkCompleted(rideId);
        
        // Gửi thông báo cho tất cả hành khách
        for (Booking booking : bookings) {
            notificationService.sendNotification(
                booking.getPassenger().getEmail(),
                "Chuyến đi đã hoàn thành",
                "Tài xế đã xác nhận hoàn thành chuyến đi. Vui lòng xác nhận từ phía bạn để hoàn tất.",
                "RIDE_COMPLETED",
                booking.getId()
            );
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Tài xế đã hoàn thành chuyến đi", null));
    }



}
