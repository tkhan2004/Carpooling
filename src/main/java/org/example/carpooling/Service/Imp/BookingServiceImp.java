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
import java.util.stream.Collectors;

@Service
public class BookingServiceImp implements BookingService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    FileServiceImp fileService;

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

        return new BookingDTO(booking, fileService);
    }

    @Override
    public BookingDTO passengerConfirm(Long rideId, String email) {
        Users passenger = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hành khách"));

        List<Booking> bookings = bookingRepository.findByRides_IdAndPassenger_Id(rideId, passenger.getId());
        if (bookings.isEmpty()) {
            throw new RuntimeException("Không tìm thấy booking cho hành khách này");
        }

        Booking booking = bookings.get(0);

        // Xử lý trạng thái xác nhận
        if (booking.getStatus() == BookingStatus.DRIVER_CONFIRMED) {
            booking.setStatus(BookingStatus.COMPLETED);
        } else if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.PASSENGER_CONFIRMED);
        }

        bookingRepository.save(booking);

        // Nếu tất cả booking đã hoàn tất => set ride thành COMPLETED
        List<Booking> allBookings = bookingRepository.findByRidesId(rideId);
        boolean allCompleted = allBookings.stream()
                .allMatch(b -> b.getStatus() == BookingStatus.COMPLETED);

        if (allCompleted) {
            Rides ride = booking.getRides();
            ride.setStatus(RideStatus.COMPLETED);
            rideRepository.save(ride);
        }

        return new BookingDTO(booking, fileService);
    }

    @Override
    public void driverMarkCompleted(Long rideId) {
        Rides ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến đi"));

        List<Booking> bookings = bookingRepository.findByRidesId(rideId);

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.PASSENGER_CONFIRMED) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.DRIVER_CONFIRMED);
            }
            bookingRepository.save(booking);
        }

        boolean allCompleted = bookings.stream()
                .allMatch(b -> b.getStatus() == BookingStatus.COMPLETED);

        if (allCompleted) {
            ride.setStatus(RideStatus.COMPLETED);
        } else {
            ride.setStatus(RideStatus.DRIVER_CONFIRMED);
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

        booking.setStatus(BookingStatus.ACCEPTED);
        bookingRepository.save(booking);
    }

    @Override
    public List<BookingDTO> getBookingsForDriver(String email) {
        Users driver = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        List<Rides> rides = rideRepository.findByDriver_Email(email);
        List<Booking> bookings = bookingRepository.findAllByRidesIn(rides);

        return bookings.stream().map(b -> new BookingDTO(b, fileService)).collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getBookingsForPassenger(String email) {
        List<Booking> bookings = bookingRepository.findBookingsByPassengerEmail(email);

        return bookings.stream()
                .map(booking -> new BookingDTO(booking, fileService))
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO cancleBookings(Long rideId, String email) {
        Users passenger = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hành khách"));

        List<Booking> bookings = bookingRepository.findByRides_IdAndPassenger_Id(rideId, passenger.getId());

        if (bookings.isEmpty()) {
            throw new RuntimeException("Không tìm thấy booking cho hành khách này");
        }

        Booking booking = bookings.get(0);
        Rides ride = booking.getRides();
        ride.setAvailableSeats(ride.getAvailableSeats() + booking.getSeatsBooked());
        rideRepository.save(ride);

        booking.setStatus(BookingStatus.PASSENGER_CONFIRMED);
        bookingRepository.save(booking);

        if (booking.getRides().getStatus() == RideStatus.DRIVER_CONFIRMED) {
            booking.getRides().setStatus(RideStatus.COMPLETED);
            rideRepository.save(booking.getRides());
        }

        return new BookingDTO(booking, fileService);
    }

    @Override
    public List<BookingDTO> getBookingsForPassengerByStatus(String passengerEmail, List<BookingStatus> statuses) {
        return List.of();
    }

    @Override
    public List<BookingDTO> getBookingsForDriverByStatus(String driverEmail, List<BookingStatus> statuses) {
        return List.of();
    }
}
