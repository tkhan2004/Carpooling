package org.example.carpooling.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Dto.UserDTO;
import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Status.BookingStatus;
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
@Tag(name = "Passenger", description = "API dành cho hành khách")
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


    @Operation(summary = "Lấy danh sách đặt chỗ của hành khách", 
            description = "Trả về danh sách các đặt chỗ của hành khách đã đăng nhập")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/bookings")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getPassengerBookings(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String passengerEmail = jwtUtil.extractUsername(token);
            List<BookingDTO> bookings = bookingService.getBookingsForPassenger(passengerEmail);

            long pendingCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
            long inGoing = bookings.stream().filter(b -> b.getStatus() == BookingStatus.IN_PROGRESS).count();
            long acceptedCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.ACCEPTED).count();
            long completedCount = bookings.stream().filter(b ->
                    b.getStatus() == BookingStatus.PASSENGER_CONFIRMED ||
                            b.getStatus() == BookingStatus.DRIVER_CONFIRMED ||
                            b.getStatus() == BookingStatus.COMPLETED).count();

            String message = String.format("Danh sách bookings của khách hàng (Chờ xác nhận: %d, Đã chấp nhận: %d, Đang đi: %d ,Đã hoàn thành: %d)",
                    pendingCount, acceptedCount, inGoing,completedCount);

            return ResponseEntity.ok(new ApiResponse<>(true, message, HttpStatus.OK.value(), bookings));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy danh sách bookings", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Lấy thông tin cá nhân hành khách", 
            description = "Trả về thông tin chi tiết của hành khách đã đăng nhập")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/profile")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String username = jwtUtil.extractUsername(token);
            Optional<Users> users = userRepository.findByEmail(username);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND.value(), null));
            }

            Users user = users.get();
            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .avatarUrl(user.getAvatarImage())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .phoneNumber(user.getPhone())
                    .role(user.getRole().getName())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(true, "Thông tin người dùng", HttpStatus.OK.value(), userDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy thông tin người dùng", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Đặt chỗ cho chuyến đi", 
            description = "Hành khách đặt chỗ cho một chuyến đi cụ thể")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đặt chỗ thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Đặt chỗ thất bại")
    })
    @PostMapping("/booking/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> bookRides(
            @Parameter(description = "ID của chuyến đi cần đặt chỗ") 
            @PathVariable Long rideId, 
            @Parameter(description = "Số ghế cần đặt") 
            @RequestParam int seats, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String username = jwtUtil.extractUsername(token);

            BookingDTO bookingDTO = bookingService.bookRide(rideId, seats, username);
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingDTO.getId());

            bookingOpt.ifPresent(booking -> notificationService.sendNotification(
                    booking.getRides().getDriver().getEmail(),
                    "Có đặt chỗ mới",
                    "Hành khách " + booking.getPassenger().getFullName() + " đã đặt " + seats + " chỗ cho chuyến đi từ "
                            + booking.getRides().getDeparture() + " đến " + booking.getRides().getDestination(),
                    "NEW_BOOKING",
                    booking.getId()
            ));

            return ResponseEntity.ok(new ApiResponse<>(true, "Đăng ký chuyến đi thành công", HttpStatus.OK.value(), bookingDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, "Đăng ký chuyến đi thất bại", HttpStatus.BAD_REQUEST.value(), null));
        }
    }

    @Operation(summary = "Xác nhận hoàn thành chuyến đi", 
            description = "Hành khách xác nhận đã hoàn thành chuyến đi")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xác nhận thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Xác nhận thất bại")
    })
    @PutMapping("/passenger-confirm/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> passengerConfirm(
            @Parameter(description = "ID của chuyến đi cần xác nhận") 
            @PathVariable Long rideId, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String username = jwtUtil.extractUsername(token);

            bookingService.passengerConfirm(rideId, username);

            List<Booking> bookingList = bookingRepository.findByRides_IdAndPassenger_Email(rideId, username);
            if (!bookingList.isEmpty()) {
                Booking booking = bookingList.get(0);
                notificationService.sendNotification(
                        booking.getRides().getDriver().getEmail(),
                        "Hành khách đã xác nhận hoàn thành",
                        "Hành khách " + booking.getPassenger().getFullName() + " đã xác nhận hoàn thành chuyến đi.",
                        "PASSENGER_CONFIRMED",
                        booking.getId()
                );
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Hành khách đã xác nhận hoàn thành", HttpStatus.OK.value()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, "Lỗi khi xác nhận", HttpStatus.BAD_REQUEST.value()));
        }
    }

    @Operation(summary = "Lấy chi tiết đặt chỗ", 
            description = "Trả về thông tin chi tiết của một đặt chỗ cụ thể")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy booking"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi máy chủ")
    })
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingDetail(
            @Parameter(description = "ID của booking cần xem chi tiết") 
            @PathVariable Long bookingId, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String email = jwtUtil.extractUsername(token);

            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

            if (bookingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Không tìm thấy booking", HttpStatus.NOT_FOUND.value(), null));
            }

            Booking booking = bookingOpt.get();
            if (!booking.getPassenger().getEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(false, "Bạn không có quyền truy cập booking này", HttpStatus.FORBIDDEN.value(), null));
            }

            BookingDTO dto = new BookingDTO(booking);
            String message;
            switch (dto.getStatus()) {
                case PENDING:
                    message = "Chi tiết booking - Đang chờ tài xế xác nhận"; break;
                case ACCEPTED:
                    message = "Chi tiết booking - Tài xế đã chấp nhận chuyến đi"; break;
                case PASSENGER_CONFIRMED:
                    message = "Chi tiết booking - Bạn đã xác nhận hoàn thành chuyến đi"; break;
                case DRIVER_CONFIRMED:
                    message = "Chi tiết booking - Tài xế đã xác nhận hoàn thành, đang chờ bạn xác nhận"; break;
                case COMPLETED:
                    message = "Chi tiết booking - Chuyến đi đã hoàn thành"; break;
                case CANCELLED:
                    message = "Chi tiết booking - Chuyến đi đã bị hủy"; break;
                case REJECTED:
                    message = "Chi tiết booking - Tài xế đã từ chối chuyến đi"; break;
                default:
                    message = "Chi tiết booking";
            }

            return ResponseEntity.ok(new ApiResponse<>(true, message, HttpStatus.OK.value(), dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Lỗi khi lấy bookings", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @Operation(summary = "Hủy đặt chỗ", 
            description = "Hành khách hủy đặt chỗ cho một chuyến đi")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hủy đặt chỗ thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Hủy đặt chỗ thất bại")
    })
    @PutMapping("/cancel-bookings/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(
            @Parameter(description = "ID của chuyến đi cần hủy đặt chỗ") 
            @PathVariable Long rideId, 
            @Parameter(description = "HTTP request chứa JWT token") 
            HttpServletRequest request) {
        try {
            String token = jwtUtil.extractTokenFromRequest(request);
            String username = jwtUtil.extractUsername(token);

            List<Booking> bookingList = bookingRepository.findByRides_IdAndPassenger_Email(rideId, username);
            BookingDTO bookingDTO = bookingService.cancelBookings(rideId, username);

            if (!bookingList.isEmpty()) {
                Booking booking = bookingList.get(0);
                notificationService.sendNotification(
                        booking.getRides().getDriver().getEmail(),
                        "Hành khách đã hủy booking",
                        "Hành khách " + booking.getPassenger().getFullName() + " đã hủy đặt chỗ cho chuyến đi từ "
                                + booking.getRides().getDeparture() + " đến " + booking.getRides().getDestination(),
                        "BOOKING_CANCELLED",
                        booking.getId()
                );
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Huỷ chuyến đi thành công", HttpStatus.OK.value(), bookingDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(false, "Huỷ chuyến đi thất bại", HttpStatus.BAD_REQUEST.value(), null));
        }
    }

}
