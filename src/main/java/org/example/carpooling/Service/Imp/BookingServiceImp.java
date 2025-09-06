package org.example.carpooling.Service.Imp;

import jakarta.transaction.Transactional;
import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Dto.PassengerInfoDTO;
import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Entity.Status.RideStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingServiceImp implements BookingService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    BookingRepository bookingRepository;

    // Khách book
    @Override
    @Transactional
    public BookingDTO bookRide(Long rideId, int seats, String email) {
        // 1. Lấy passenger từ email
        Users passenger = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. Lấy ride
        Rides ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));

        // ⚠️ 3. Kiểm tra người dùng đã có booking chưa hoàn tất chưa
        boolean alreadyBooked = bookingRepository.existsByPassengerAndRidesAndStatusIn(
                passenger, ride, List.of(BookingStatus.PENDING, BookingStatus.ACCEPTED)
        );

        if (alreadyBooked) {
            throw new RuntimeException("Bạn đã đặt chuyến này rồi và đang chờ hoặc đã được xác nhận.");
        }

        // 4. Kiểm tra ghế
        if (seats > ride.getAvailableSeats()) {
            throw new RuntimeException("Không đủ ghế trống cho chuyến đi này");
        }

        // 5. Tạo booking mới
        Booking booking = new Booking();
        booking.setRides(ride);
        booking.setPassenger(passenger);
        booking.setSeatsBooked(seats);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        return new BookingDTO(booking);
    }

    @Override
    public BookingDTO passengerConfirm(Long rideId, String email) {
        // Tìm booking gần nhất chưa bị hủy của hành khách cho chuyến đi này
        Booking booking = bookingRepository.findTopByRides_IdAndPassenger_EmailAndStatusNot(
                        rideId, email, BookingStatus.CANCELLED)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking hợp lệ cho hành khách này"));

        // Chỉ cho phép xác nhận khi chuyến đi đang diễn ra
        if (booking.getStatus() != BookingStatus.IN_PROGRESS && booking.getStatus() != BookingStatus.DRIVER_CONFIRMED) {
            throw new IllegalStateException("Không thể xác nhận hoàn thành khi booking không ở trạng thái IN_PROGRESS hoặc DRIVER_CONFIRMED.");
        }

        // Xử lý trạng thái xác nhận

        if (booking.getStatus() == BookingStatus.DRIVER_CONFIRMED
                || booking.getRides().getStatus() == RideStatus.DRIVER_CONFIRMED) {
            booking.setStatus(BookingStatus.COMPLETED);
        } else {
            booking.setStatus(BookingStatus.PASSENGER_CONFIRMED);
        }

        bookingRepository.save(booking);

        // Kiểm tra và cập nhật trạng thái của Ride sau khi booking thay đổi
        updateRideStatusAfterBookingChange(rideId);

        return new BookingDTO(booking);
    }

    @Override
    public void driverMarkCompleted(Long rideId) {
        Rides ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));

        // Chỉ cho phép tài xế xác nhận khi chuyến đi đang diễn ra
        if (ride.getStatus() != RideStatus.IN_PROGRESS) {
            throw new IllegalStateException("Không thể xác nhận hoàn thành khi chuyến đi không ở trạng thái IN_PROGRESS.");
        }

        List<Booking> bookings = bookingRepository.findByRidesIdAndStatusNot(rideId, BookingStatus.CANCELLED);

        if (bookings.isEmpty()) {
            // Nếu không có booking nào, chuyến đi có thể được xem là hoàn thành ngay lập tức
            ride.setStatus(RideStatus.COMPLETED);
            rideRepository.save(ride);
            return; // Kết thúc sớm
        }

        // Lặp qua các booking hợp lệ và cập nhật trạng thái
        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.PASSENGER_CONFIRMED) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
                booking.setStatus(BookingStatus.DRIVER_CONFIRMED);
            }
            bookingRepository.save(booking);
        }

        // Kiểm tra và cập nhật trạng thái cuối cùng của Ride
        updateRideStatusAfterBookingChange(rideId);
    }

    /**
     * Hàm helper để kiểm tra tất cả booking (không bị hủy) của một chuyến đi
     * và cập nhật trạng thái của chuyến đi đó thành COMPLETED hoặc WAITING_CONFIRMATION.
     *
     * @param rideId ID của chuyến đi cần kiểm tra.
     */
    private void updateRideStatusAfterBookingChange(Long rideId) {
        Rides ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));

        List<Booking> activeBookings = bookingRepository.findByRidesId(rideId).stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED && b.getStatus() != BookingStatus.REJECTED)
                .collect(Collectors.toList());

        if (activeBookings.isEmpty()) {
            ride.setStatus(RideStatus.COMPLETED);
        } else {
            boolean allCompleted = activeBookings.stream()
                    .allMatch(b -> b.getStatus() == BookingStatus.COMPLETED);

            if (allCompleted) {
                // Nếu tất cả booking xong → ride hoàn thành
                ride.setStatus(RideStatus.COMPLETED);
            } else {
                // Nếu chưa xong nhưng có booking driver đã confirm → ride ở DRIVER_CONFIRMED
                boolean anyDriverConfirmed = activeBookings.stream()
                        .anyMatch(b -> b.getStatus() == BookingStatus.DRIVER_CONFIRMED
                                || b.getStatus() == BookingStatus.COMPLETED);

                if (anyDriverConfirmed) {
                    ride.setStatus(RideStatus.DRIVER_CONFIRMED);
                } else {
                    // Nếu tất cả vẫn đang IN_PROGRESS → giữ IN_PROGRESS
                    ride.setStatus(RideStatus.IN_PROGRESS);
                }
            }
        }

        rideRepository.save(ride);
    }

    @Override
    @Transactional
    public void driverAcceptBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt booking đang ở trạng thái PENDING");
        }

        Rides ride = booking.getRides();
        int remainingSeats = ride.getAvailableSeats() - booking.getSeatsBooked();
        if (remainingSeats < 0) {
            throw new RuntimeException("Không đủ ghế trống để duyệt booking này");
        }

        ride.setAvailableSeats(remainingSeats);

        rideRepository.save(ride);

        if(booking.getStatus() == BookingStatus.REJECTED){
            System.out.println("Không thể acccept");
        }else{
            booking.setStatus(BookingStatus.ACCEPTED);
        }
        bookingRepository.save(booking);
    }


    @Override
    public List<BookingDTO> getBookingsForPassenger(String email) {
        List<Booking> bookings = bookingRepository.findBookingsByPassengerEmail(email);

        return bookings.stream()
                .map(booking -> new BookingDTO(booking))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingDTO cancelBookings(Long rideId, String email) {
        Users passenger = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hành khách"));

        // Chỉ tìm các booking CHƯA bị hủy hoặc từ chối
        List<Booking> activeBookings = bookingRepository.findByRides_IdAndPassenger_IdAndStatusNotInOrderByCreatedAtDesc(
                rideId,
                passenger.getId(),
                Arrays.asList(BookingStatus.CANCELLED, BookingStatus.REJECTED, BookingStatus.COMPLETED)
        );

        if (activeBookings.isEmpty()) {
            throw new RuntimeException("Không tìm thấy booking đang hoạt động cho hành khách này");
        }

        Booking cancelledBooking = null;

        for (Booking booking : activeBookings) {
            BookingStatus status = booking.getStatus();

            // Chỉ xử lý CANCEL nếu booking chưa hoàn thành
            if (status == BookingStatus.ACCEPTED || status == BookingStatus.PENDING) {

                // Nếu đã được tài xế ACCEPT thì phải hoàn lại ghế
                if (status == BookingStatus.ACCEPTED) {
                    Rides ride = booking.getRides();
                    ride.setAvailableSeats(ride.getAvailableSeats() + booking.getSeatsBooked());
                    rideRepository.save(ride);
                }

                booking.setStatus(BookingStatus.CANCELLED);
                cancelledBooking = bookingRepository.save(booking); // Lưu trạng thái huỷ

                // Chỉ hủy booking mới nhất
                break;
            } else if (status == BookingStatus.IN_PROGRESS) {
                throw new RuntimeException("Không thể hủy chuyến đi đang diễn ra");
            } else if (status == BookingStatus.PASSENGER_CONFIRMED || status == BookingStatus.DRIVER_CONFIRMED) {
                throw new RuntimeException("Không thể hủy chuyến đi đã được xác nhận");
            }
        }

        if (cancelledBooking == null) {
            throw new RuntimeException("Không có booking nào đủ điều kiện để huỷ");
        }

        return new BookingDTO(cancelledBooking);
    }

    @Override
    public List<BookingDTO> getBookingsForPassengerByStatus(String passengerEmail, List<BookingStatus> statuses) {
        return List.of();
    }

    @Override
    public List<BookingDTO> getBookingsForDriver(String email) {
        Users driver = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế với email: " + email));

        List<Rides> driverRides = rideRepository.findByDriver_Email(email);

        // Thu thập tất cả bookings theo từng chuyến đi
        Map<Long, List<Booking>> rideToBookingsMap = new HashMap<>();
        List<Booking> allDriverBookings = new ArrayList<>();

        for (Rides ride : driverRides) {
            List<Booking> bookingsForRide = bookingRepository.findByRidesId(ride.getId());
            rideToBookingsMap.put(ride.getId(), bookingsForRide);
            allDriverBookings.addAll(bookingsForRide);
        }

        // Chuyển đổi sang BookingDTO và thêm thông tin hành khách cùng chuyến
        return allDriverBookings.stream().map(booking -> {
            BookingDTO dto = new BookingDTO(booking);

            // Thêm danh sách hành khách cùng chuyến, BAO GỒM tất cả trạng thái
            List<Booking> sameRideBookings = rideToBookingsMap.get(booking.getRides().getId());
            List<PassengerInfoDTO> fellowPassengers = sameRideBookings.stream()
                    .filter(b -> !b.getId().equals(booking.getId())) // Loại trừ booking hiện tại
                    .map(b -> new PassengerInfoDTO(b))
                    .collect(Collectors.toList());

            dto.setFellowPassengers(fellowPassengers);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsForDriverByStatus(String driverEmail, List<BookingStatus> statuses) {
        Users driver = userRepository.findByEmail(driverEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế với email: " + driverEmail));

        List<Rides> driverRides = rideRepository.findByDriver_Email(driverEmail);

        // Tìm tất cả bookings của tài xế theo trạng thái đã chỉ định
        List<Booking> driverBookings = new ArrayList<>();
        Map<Long, List<Booking>> rideToBookingsMap = new HashMap<>();

        for (Rides ride : driverRides) {
            // Lấy tất cả bookings cho ride, không phụ thuộc vào trạng thái
            List<Booking> allBookingsForRide = bookingRepository.findByRidesId(ride.getId());
            // Lấy các bookings theo trạng thái được yêu cầu
            List<Booking> filteredBookings = bookingRepository.findByRidesIdAndStatusIn(ride.getId(), statuses);

            driverBookings.addAll(filteredBookings);

            // Lưu TẤT CẢ bookings (không phụ thuộc vào trạng thái) theo rideId
            rideToBookingsMap.put(ride.getId(), allBookingsForRide);
        }

        // Chuyển đổi từ Booking sang BookingDTO với thông tin về hành khách cùng chuyến
        return driverBookings.stream().map(booking -> {
            BookingDTO dto = new BookingDTO(booking);

            // Thêm danh sách hành khách cùng chuyến, BAO GỒM tất cả trạng thái
            List<Booking> sameRideBookings = rideToBookingsMap.get(booking.getRides().getId());
            List<PassengerInfoDTO> fellowPassengers = sameRideBookings.stream()
                    .filter(b -> !b.getId().equals(booking.getId())) // Loại trừ booking hiện tại
                    .map(b -> new PassengerInfoDTO(b))
                    .collect(Collectors.toList()).reversed();

            dto.setFellowPassengers(fellowPassengers);
            return dto;
        }).collect(Collectors.toList());
    }
}
