package org.example.carpooling.Controller;

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
import org.example.carpooling.Service.Imp.FileServiceImp;
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

    @Autowired
    FileServiceImp fileServiceImp;

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getPassengerBookings(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String passengerEmail = jwtUtil.extractUsername(token);
        
        // Lấy danh sách booking của hành khách
        List<BookingDTO> bookings = bookingService.getBookingsForPassenger(passengerEmail);
        
        // Phân loại bookings theo trạng thái để cung cấp thông tin thống kê
        long pendingCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.PENDING).count();
        long acceptedCount = bookings.stream().filter(b -> b.getStatus() == BookingStatus.ACCEPTED).count();
        long completedCount = bookings.stream().filter(b -> 
                b.getStatus() == BookingStatus.PASSENGER_CONFIRMED || 
                b.getStatus() == BookingStatus.DRIVER_CONFIRMED || 
                b.getStatus() == BookingStatus.COMPLETED).count();
        
        String message = String.format("Danh sách bookings của khách hàng (Chờ xác nhận: %d, Đã chấp nhận: %d, Đã hoàn thành: %d)",
                pendingCount, acceptedCount, completedCount);
        
        return ResponseEntity.ok(new ApiResponse<>(true, message, bookings));
    }


    @GetMapping("/profile")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);

        String username = jwtUtil.extractUsername(token);
        Optional<Users> users =  userRepository.findByEmail(username);

        Users user = users.get();
        UserDTO userDTO = new UserDTO(user, fileServiceImp); // ✅ dùng constructor đã xử lý avatarUrl đúng
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


    @PutMapping("/passenger-confirm/{rideId}")
    @PreAuthorize("hasAnyRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> passengerConfirm(@PathVariable Long rideId, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);

        // Xác nhận hoàn thành
        bookingService.passengerConfirm(rideId, username);

        // Lấy booking để gửi thông báo
        List<Booking> bookingList = bookingRepository.findByRides_IdAndPassenger_Email(rideId, username);
        if (!bookingList.isEmpty()) {
            Booking booking = bookingList.get(0); // hoặc loop nếu cần gửi nhiều

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



    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingDetail(@PathVariable Long bookingId, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String email = jwtUtil.extractUsername(token);

        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);

        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Không tìm thấy booking", null));
        }

        Booking booking = bookingOpt.get();

        if (!booking.getPassenger().getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, "Bạn không có quyền truy cập booking này", null));
        }

        // Sử dụng BookingDTO chi tiết
        BookingDTO dto = new BookingDTO(booking, fileServiceImp);
        
        // Thông báo tùy chỉnh dựa trên trạng thái
        String message;
        switch(dto.getStatus()) {
            case PENDING:
                message = "Chi tiết booking - Đang chờ tài xế xác nhận";
                break;
            case ACCEPTED:
                message = "Chi tiết booking - Tài xế đã chấp nhận chuyến đi";
                break;
            case PASSENGER_CONFIRMED:
                message = "Chi tiết booking - Bạn đã xác nhận hoàn thành chuyến đi";
                break;
            case DRIVER_CONFIRMED:
                message = "Chi tiết booking - Tài xế đã xác nhận hoàn thành, đang chờ bạn xác nhận";
                break;
            case COMPLETED:
                message = "Chi tiết booking - Chuyến đi đã hoàn thành";
                break;
            case CANCELLED:
                message = "Chi tiết booking - Chuyến đi đã bị hủy";
                break;
            case REJECTED:
                message = "Chi tiết booking - Tài xế đã từ chối chuyến đi";
                break;
            default:
                message = "Chi tiết booking";
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, message, dto));
    }

    @PutMapping("/cancel-bookings/{rideId}")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<ApiResponse<BookingDTO>> cancelBooking(@PathVariable Long rideId, HttpServletRequest request) {
        String token = jwtUtil.extractTokenFromRequest(request);
        String username = jwtUtil.extractUsername(token);

        // Lấy booking trước khi hủy
        List<Booking> bookingList = bookingRepository.findByRides_IdAndPassenger_Email(rideId, username);
        // Hủy booking
        BookingDTO bookingDTO = bookingService.cancelBookings(rideId, username);

        // Gửi thông báo cho tài xế nếu booking tồn tại
        if (!bookingList.isEmpty()) {
            Booking booking = bookingList.get(0); // Hoặc chọn đúng booking cần thông báo
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
