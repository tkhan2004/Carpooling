package org.example.carpooling.Service.Imp;

import jakarta.transaction.Transactional;
import org.example.carpooling.Dto.BookingDTO;
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
import java.util.List;
import java.util.Optional;
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

        // 3. Kiểm tra xem có còn đủ ghế không
        if (seats > ride.getAvailable_seats()) {
            throw new RuntimeException("Không đủ ghế trống cho chuyến đi này");
        }

        // 4. Tạo booking mới với trạng thái PENDING
        Booking booking = new Booking();
        booking.setRides(ride);
        booking.setPassenger(passenger);
        booking.setSeatsBooked(seats);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        // 5. Lưu booking (chưa trừ ghế)
        bookingRepository.save(booking);

        // 6. Trả về DTO
        return new BookingDTO(booking);
    }


    // Khách xác nhận thành công
    @Override
    public BookingDTO passengerConfirm(Long rideId, String email) {
        Users passenger = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hành khách"));

        // Truy vấn danh sách booking phù hợp
        List<Booking> bookings = bookingRepository.findByRides_IdAndPassenger_Id(rideId, passenger.getId());

        if (bookings.isEmpty()) {
            throw new RuntimeException("Không tìm thấy booking cho hành khách này");
        }

        // Giả sử chỉ lấy booking đầu tiên (nếu bạn đã giới hạn 1 người chỉ được book 1 lần)
        Booking booking = bookings.get(0);

        booking.setStatus(BookingStatus.PASSENGER_CONFIRMED);
        bookingRepository.save(booking);

        // Nếu tài xế đã xác nhận → cập nhật Ride thành COMPLETED
        if (booking.getRides().getStatus() == RideStatus.DRIVER_CONFIRMED) {
            booking.getRides().setStatus(RideStatus.COMPLETED);
            rideRepository.save(booking.getRides());
        }

        return new BookingDTO(booking);
    }

    //Tài xế xác nhận thành công
    @Override
    public void driverMarkCompleted(Long rideId) {
        Rides ride = rideRepository.findById(rideId).orElseThrow();
        ride.setStatus(RideStatus.DRIVER_CONFIRMED);

        Optional<Booking> bookings = bookingRepository.findById(rideId);
        boolean allPassengerConfirmed = bookings.stream()
                .allMatch(b -> b.getStatus() == BookingStatus.PASSENGER_CONFIRMED);

        if (allPassengerConfirmed) {
            ride.setStatus(RideStatus.COMPLETED);
        }

        rideRepository.save(ride);
    }

    //
    @Override
    @Transactional
    public void driverAcceptBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể duyệt booking đang ở trạng thái PENDING");
        }

        // Trừ ghế
        Rides ride = booking.getRides();
        int remainingSeats = ride.getAvailable_seats() - booking.getSeatsBooked();
        if (remainingSeats < 0) {
            throw new RuntimeException("Không đủ ghế trống để duyệt booking này");
        }

        ride.setAvailable_seats(remainingSeats);
        rideRepository.save(ride); // nhớ lưu lại ride

        // Cập nhật trạng thái booking
        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);
    }

    @Override
    public List<BookingDTO> getBookingsForDriver(String email) {
        Users driver = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Lấy danh sách tất cả ride của driver đó
        List<Rides> rides = rideRepository.findByDriver_Email(email);

        // Lấy tất cả booking thuộc những ride đó
        List<Booking> bookings = bookingRepository.findAllByRidesIn(rides);

        return bookings.stream().map(BookingDTO::new).collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsForPassenger(String email) {
        Users driver = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        // Lấy danh sách tất cả ride của driver đó
        List<Rides> rides = rideRepository.findByDriver_Email(email);

        // Lấy tất cả booking thuộc những ride đó
        List<Booking> bookings = bookingRepository.findAllByRidesIn(rides);

        return bookings.stream().map(BookingDTO::new).collect(Collectors.toList());
    }

    @Override
    public BookingDTO cancleBookings(Long rideId, String email) {
        Users passenger = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hành khách"));

        // Truy vấn danh sách booking phù hợp
        List<Booking> bookings = bookingRepository.findByRides_IdAndPassenger_Id(rideId, passenger.getId());

        if (bookings.isEmpty()) {
            throw new RuntimeException("Không tìm thấy booking cho hành khách này");
        }

        // Giả sử chỉ lấy booking đầu tiên (nếu bạn đã giới hạn 1 người chỉ được book 1 lần)
        Booking booking = bookings.get(0);

        booking.setStatus(BookingStatus.PASSENGER_CONFIRMED);
        bookingRepository.save(booking);

        // Nếu tài xế đã xác nhận → cập nhật Ride thành COMPLETED
        if (booking.getRides().getStatus() == RideStatus.DRIVER_CONFIRMED) {
            booking.getRides().setStatus(RideStatus.COMPLETED);
            rideRepository.save(booking.getRides());
        }

        return new BookingDTO(booking);

    }
}
