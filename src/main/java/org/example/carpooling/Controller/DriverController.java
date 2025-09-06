package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Dto.DriverDTO;
import org.example.carpooling.Dto.Request.RideRequestDTO;
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
@Tag(name = "Driver", description = "API dành cho tài xế")
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

    @Operation(summary = "Lấy thông tin cá nhân tài xế", 
            description = "Trả về thông tin chi tiết của tài xế đã đăng nhập")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Không có quyền truy cập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('DRIVER')")
    public ResponseEntity<?> getProfile(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
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

            return ResponseEntity.ok(new ApiResponse<>(true, "Thông tin cá nhân tài xế", HttpStatus.OK.value(), driverDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy thông tin cá nhân", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Lấy danh sách chuyến đi của tài xế", 
            description = "Trả về danh sách các chuyến đi do tài xế đã đăng nhập tạo ra")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/my-rides")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<List<RideRequestDTO>>> getRide(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);
            List<RideRequestDTO> rides = rideService.getRidesByDriverEmail(email);
            ApiResponse<List<RideRequestDTO>> response = new ApiResponse<>(true, "Danh sách chuyến đi", HttpStatus.OK.value(), rides);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lấy danh sách thất bại", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Lấy danh sách đặt chỗ của tài xế", 
            description = "Trả về danh sách các đặt chỗ cho các chuyến đi của tài xế đã đăng nhập")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getDriverBookings(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
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

            return ResponseEntity.ok(new ApiResponse<>(true, message, HttpStatus.OK.value(), bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy danh sách bookings", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Chấp nhận đặt chỗ", 
            description = "Tài xế chấp nhận yêu cầu đặt chỗ của hành khách")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Chấp nhận thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy booking"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @PutMapping("/accept/{bookingId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverAcceptBooking(
            @Parameter(description = "ID của booking cần chấp nhận") 
            @PathVariable Long bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Không tìm thấy booking", HttpStatus.NOT_FOUND.value(), null));
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

            return ResponseEntity.ok(new ApiResponse<>(true, "Đã chấp nhận hành khách", HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi chấp nhận hành khách", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "Từ chối đặt chỗ", 
            description = "Tài xế từ chối yêu cầu đặt chỗ của hành khách")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Từ chối thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy booking"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @PutMapping("/reject/{bookingId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverRejectBooking(
            @Parameter(description = "ID của booking cần từ chối") 
            @PathVariable Long bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Không tìm thấy booking", HttpStatus.NOT_FOUND.value(), null));
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

            return ResponseEntity.ok(new ApiResponse<>(true, "Đã từ chối hành khách", HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi không thể từ chối hành khách", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "Xác nhận hoàn thành chuyến đi", 
            description = "Tài xế xác nhận đã hoàn thành chuyến đi")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xác nhận thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @PutMapping("/complete/{rideId}")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<ApiResponse<String>> driverMarkCompleted(
            @Parameter(description = "ID của chuyến đi cần xác nhận hoàn thành") 
            @PathVariable Long rideId) {
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

            return ResponseEntity.ok(new ApiResponse<>(true, "Tài xế đã hoàn thành chuyến đi", HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi xác nhận chuyến đi", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }


}
