package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Entity.Status.DriverStatus;
import org.example.carpooling.Entity.Status.RideStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.example.carpooling.Entity.Status.DriverStatus.PENDING;

@Service
public class RideServiceImp implements RideService {

    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    BookingServiceImp bookingServiceImp;

    private final UserRepository userRepository;
    private final RideRepository rideRepository;

    @Autowired
    public RideServiceImp(UserRepository userRepository, RideRepository rideRepository) {
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;

    }

    @Override
    public void createRide(RideRequestDTO rideRequest, String email) {
        Users driver = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế"));

        if (!driver.getRole().getName().equals("DRIVER")) {
            throw new RuntimeException("Chỉ tài xế mới có thể tạo chuyến đi");
        }

        if (driver.getStatus() == DriverStatus.PENDING || driver.getStatus() == DriverStatus.REJECTED) {
            throw new RuntimeException("Tài xế chưa được duyệt");
        }

        Rides ride = new Rides();
        ride.setDriver(driver);
        ride.setDeparture(rideRequest.getDeparture());
        ride.setDestination(rideRequest.getDestination());
        ride.setStart_time(rideRequest.getStartTime());
        ride.setPrice_per_seat(rideRequest.getPricePerSeat());
        ride.setAvailable_seats(rideRequest.getTotalSeat());
        ride.setTotal_seats(rideRequest.getTotalSeat());

        rideRepository.save(ride);
    }


    @Override
    public List<RideRequestDTO> getAllRideActive() {
        return rideRepository.findAllRidesByStatus();
    }

    @Override
    public List<RideRequestDTO> getRidesByDriverEmail(String email) {
        // 1. Lấy danh sách rides từ repository
        List<Rides> rides = rideRepository.findByDriver_Email(email);

        // 2. Kiểm tra danh sách rỗng
        if (rides.isEmpty()) {
            return Collections.emptyList(); // Trả về list rỗng thay vì null
        }

        // 3. Sử dụng Stream API để map sang DTO
        return rides.stream()
                .map(ride -> new RideRequestDTO(
                        ride.getId(),
                        ride.getAvailable_seats(), // Sửa thành camelCase
                        ride.getDriver().getFullName(),
                        ride.getDriver().getEmail(),
                        ride.getDeparture(),
                        ride.getDestination(),
                        ride.getStart_time(), // Sửa thành camelCase
                        ride.getPrice_per_seat(), // Sửa thành camelCase
                        ride.getTotal_seats(), // Sửa thành camelCase
                        ride.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public RideRequestDTO findDetailRideById(Long rideId) {
        Rides ride = rideRepository.findDetailRideById(rideId);

        // Create DTO and set values using the constructor
        RideRequestDTO dto = new RideRequestDTO(
                ride.getId(),
                ride.getAvailable_seats(), // Ensure this is available in your Rides entity
                ride.getDriver().getFullName(),
                ride.getDriver().getEmail(),
                ride.getDeparture(),
                ride.getDestination(),
                ride.getStart_time(),
                ride.getPrice_per_seat(),
                ride.getTotal_seats(),
                ride.getStatus()// Make sure you get the correct field here
        );

        return dto;
    }


    @Override
    public RideRequestDTO cancelRideById(Long rideId, String email) {

        Rides ride = rideRepository.findCancelRideById(rideId);

        if ("CANCELLED".equals(ride.getStatus())) {
            throw new RuntimeException("Chuyến đi này đã bị hủy rồi");
        }

        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);

        // Cập nhật đối tượng DTO với các giá trị mới nhất từ entity Ride
        RideRequestDTO dto = new RideRequestDTO(
                ride.getId(),
                ride.getAvailable_seats(),  // Dùng 'availableSeats' thay vì 'available_seats'
                ride.getDriver().getFullName(),
                ride.getDriver().getEmail(),
                ride.getDeparture(),
                ride.getDestination(),
                ride.getStart_time(),  // Sửa lại 'getStart_time()' thành 'getStartTime()'
                ride.getPrice_per_seat(),  // 'pricePerSeat' thay vì 'price_per_seat'
                ride.getTotal_seats(),  // 'totalSeats' thay vì 'total_seats'
                ride.getStatus()  // Trả về trạng thái của chuyến đi
        );

        return dto;
    }

    @Override
    public RideRequestDTO updateRide(Long rideId, RideRequestDTO rideRequest, String email) {
        Rides ride = rideRepository.findDetailRideById(rideId);
        ride.setAvailable_seats(rideRequest.getAvailableSeats());
        ride.setDeparture(rideRequest.getDeparture());
        ride.setDestination(rideRequest.getDestination());
        ride.setStart_time(rideRequest.getStartTime());
        ride.setPrice_per_seat(rideRequest.getPricePerSeat());
        ride.setTotal_seats(rideRequest.getTotalSeat());
        rideRepository.save(ride);

        RideRequestDTO dto = new RideRequestDTO(ride.getId(),
                ride.getAvailable_seats(),
                ride.getDriver().getFullName(),
                ride.getDriver().getEmail(),
                ride.getDeparture(),
                ride.getDestination(),
                ride.getStart_time(),
                ride.getPrice_per_seat(),
                ride.getTotal_seats(),
                ride.getStatus());
        return dto;
    }

    @Override
    public List<RideRequestDTO> searchRides(String departure, String destination, LocalDate startTime, Integer seats) {
        List<Rides> rides = rideRepository.searchRides(departure, destination, startTime, seats);
        return rides.stream()
                .map(ride -> new RideRequestDTO(
                        ride.getId(),
                        ride.getAvailable_seats(),
                        ride.getDriver().getFullName(),
                        ride.getDriver().getEmail(),
                        ride.getDeparture(),
                        ride.getDestination(),
                        ride.getStart_time(),
                        ride.getPrice_per_seat(),
                        ride.getTotal_seats(),
                        ride.getStatus()
                ))
                .collect(Collectors.toList());
    }
}