package org.example.carpooling.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Dto.DriverDTO;
import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Helper.JwtUtil;
import org.example.carpooling.Payload.ApiResponse;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    UserService userService;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingService bookingService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    FileService fileService;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('DRIVER')")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
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

            return ResponseEntity.ok(new ApiResponse<>(true, "Thông tin cá nhân tài xế", driverDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy thông tin cá nhân", null));
        }
    }

    @GetMapping("/my-rides")
    @PreAuthorize(("hasRole('DRIVER')"))
    public ResponseEntity<ApiResponse<List<RideRequestDTO>>> getRide(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            List<RideRequestDTO> rides = rideService.getRidesByDriverEmail(email);
            ApiResponse<List<RideRequestDTO>> response = new ApiResponse<>(true, "Danh sách chuyến đi", rides);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lấy danh sách thất bại", null));
        }
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getDriverBookings(HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String driverEmail = jwtUtil.extractUsername(token);
            List<BookingDTO> bookings = bookingService.getBookingsForDriver(driverEmail);

            long pendingCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
            long acceptedCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.ACCEPTED).count();
            long completedCount = bookings.stream().filter(b ->
                    b.getStatus() == BookingStatus.PASSENGER_CONFIRMED ||
                            b.getStatus() == BookingStatus.DRIVER_CONFIRMED ||
                            b.getStatus() == BookingStatus.COMPLETED).count();

            int totalBookedSeats = bookings.stream()
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED && b.getStatus() != BookingStatus.REJECTED)
                    .mapToInt(BookingDTO::getSeatsBooked)
                    .sum();

            BigDecimal totalRevenue = bookings.stream()
                    .filter(b -> b.getStatus() != BookingStatus.CANCELLED && b.getStatus() != BookingStatus.REJECTED)
                    .map(BookingDTO::getTotalPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String message = String.format("Danh sách bookings của tài xế (Chờ xác nhận: %d, Đã chấp nhận: %d, Đã hoàn thành: %d, Tổng ghế đã đặt: %d, Tổng doanh thu: %s)",
                    pendingCount, acceptedCount, completedCount, totalBookedSeats, totalRevenue.toString());

            return ResponseEntity.ok(new ApiResponse<>(true, message, bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy danh sách bookings", null));
        }
    }

    @PutMapping("/accept/{bookingId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverAcceptBooking(@PathVariable Long bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Không tìm thấy booking", null));
            }

            Booking booking = bookingOpt.get();
            bookingService.driverAcceptBooking(bookingId);

            notificationService.sendNotification(
                    booking.getPassenger().getEmail(),
                    "Tài xế đã chấp nhận chuyến đi",
                    "Tài xế " + booking.getRides().getDriver().getFullName() + " đã chấp nhận chuyến đi của bạn từ " + booking.getRides().getDeparture() + " đến " + booking.getRides().getDestination(),
                    "BOOKING_ACCEPTED",
                    bookingId
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Đã chấp nhận hành khách", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi chấp nhận hành khách", null));
        }
    }

    @PutMapping("/reject/{bookingId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverRejectBooking(@PathVariable Long bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Không tìm thấy booking", null));
            }

            Booking booking = bookingOpt.get();
            booking.setStatus(BookingStatus.REJECTED);
            bookingRepository.save(booking);

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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi không thể từ chối hành khách", null));
        }
    }

    @PutMapping("/complete/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverMarkCompleted(@PathVariable Long rideId) {
        try {
            List<Booking> bookings = bookingRepository.findByRidesId(rideId);
            bookingService.driverMarkCompleted(rideId);

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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi xác nhận chuyến đi", null));
        }
    }


}
