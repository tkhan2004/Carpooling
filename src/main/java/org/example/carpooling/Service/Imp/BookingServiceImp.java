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
import org.example.carpooling.Service.FileService;
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

    @Autowired
    FileService fileService;

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

        List<Booking> bookings = bookingRepository.findByRides_IdAndPassenger_Email(rideId, email)
                .stream()
                .filter(booking -> booking.getStatus() != BookingStatus.CANCELLED)
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .collect(Collectors.toList());


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

        if (bookings.isEmpty()) {
            throw new RuntimeException("Không có hành khách nào trong chuyến đi này");
        }

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.PASSENGER_CONFIRMED) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.DRIVER_CONFIRMED);
            }
            bookingRepository.save(booking);
        }

        boolean allActive = bookings.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED) // Bỏ qua các booking đã hủy
                .allMatch(b -> b.getStatus() == BookingStatus.COMPLETED);

        if (allActive) {
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
                .map(booking -> new BookingDTO(booking, fileService))
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

        return new BookingDTO(cancelledBooking, fileService);
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
            BookingDTO dto = new BookingDTO(booking, fileService);

            // Thêm danh sách hành khách cùng chuyến, BAO GỒM tất cả trạng thái
            List<Booking> sameRideBookings = rideToBookingsMap.get(booking.getRides().getId());
            List<BookingDTO.PassengerInfo> fellowPassengers = sameRideBookings.stream()
                    .filter(b -> !b.getId().equals(booking.getId())) // Loại trừ booking hiện tại
                    .map(b -> new BookingDTO.PassengerInfo(b, fileService))
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
            BookingDTO dto = new BookingDTO(booking, fileService);

            // Thêm danh sách hành khách cùng chuyến, BAO GỒM tất cả trạng thái
            List<Booking> sameRideBookings = rideToBookingsMap.get(booking.getRides().getId());
            List<BookingDTO.PassengerInfo> fellowPassengers = sameRideBookings.stream()
                    .filter(b -> !b.getId().equals(booking.getId())) // Loại trừ booking hiện tại
                    .map(b -> new BookingDTO.PassengerInfo(b, fileService))
                    .collect(Collectors.toList());

            dto.setFellowPassengers(fellowPassengers);
            return dto;
        }).collect(Collectors.toList());
    }
}
