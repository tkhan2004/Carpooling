package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Status.DriverStatus;
import org.example.carpooling.Entity.Status.RideStatus;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
    public class RideServiceImp implements RideService {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private  RideRepository rideRepository;

    @Override
    public void createRide(RideRequestDTO rideRequest, String email) {
        Users driver = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài xế"));

        if (!"DRIVER".equals(driver.getRole().getName())) {
            throw new RuntimeException("Chỉ tài xế mới có thể tạo chuyến đi");
        }

        if (driver.getStatus() == DriverStatus.PENDING || driver.getStatus() == DriverStatus.REJECTED) {
            throw new RuntimeException("Tài xế chưa được duyệt");
        }

        LocalDateTime newStart = rideRequest.getStartTime();
        LocalDateTime newEnd = newStart.plusHours(2); // giả sử chuyến kéo dài 2 giờ

        List<Rides> existingRides = rideRepository.findByDriverAndStatus(driver, RideStatus.ACTIVE);
        for (Rides ride : existingRides) {
            LocalDateTime existingStart = ride.getStartTime();
            LocalDateTime existingEnd = existingStart.plusHours(2);
            boolean isOverlapping = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
            if (isOverlapping) {
                throw new RuntimeException("Tài xế đã có chuyến đi trùng giờ");
            }
        }

        Rides ride = new Rides();
        ride.setDriver(driver);
        ride.setDeparture(rideRequest.getDeparture());
        ride.setStartLat(rideRequest.getStartLat());
        ride.setStartLng(rideRequest.getStartLng());
        ride.setStartAddress(rideRequest.getStartAddress());
        ride.setStartWard(rideRequest.getStartWard());
        ride.setStartDistrict(rideRequest.getStartDistrict());
        ride.setStartProvince(rideRequest.getStartProvince());
        ride.setEndLat(rideRequest.getEndLat());
        ride.setEndLng(rideRequest.getEndLng());
        ride.setEndAddress(rideRequest.getEndAddress());
        ride.setEndWard(rideRequest.getEndWard());
        ride.setEndDistrict(rideRequest.getEndDistrict());
        ride.setEndProvince(rideRequest.getEndProvince());
        ride.setDestination(rideRequest.getDestination());
        ride.setStartTime(newStart);
        ride.setPricePerSeat(rideRequest.getPricePerSeat());
        ride.setAvailableSeats(rideRequest.getTotalSeat());
        ride.setTotalSeats(rideRequest.getTotalSeat());

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
                        ride.getAvailableSeats(),
                        ride.getDriver() != null ? ride.getDriver().getFullName() : null,
                        ride.getDriver() != null ? ride.getDriver().getEmail() : null,

                        // Điểm đi
                        ride.getDeparture(),
                        ride.getStartLat(),
                        ride.getStartLng(),
                        ride.getStartAddress(),
                        ride.getStartWard(),
                        ride.getStartDistrict(),
                        ride.getStartProvince(),

                        // Điểm đến
                        ride.getEndLat(),
                        ride.getEndLng(),
                        ride.getEndAddress(),
                        ride.getEndWard(),
                        ride.getEndDistrict(),
                        ride.getEndProvince(),
                        ride.getDestination(),

                        // Thời gian, giá, ghế, trạng thái
                        ride.getStartTime(),
                        ride.getPricePerSeat(),
                        ride.getTotalSeats(),
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
                ride.getAvailableSeats(),
                ride.getDriver() != null ? ride.getDriver().getFullName() : null,
                ride.getDriver() != null ? ride.getDriver().getEmail() : null,

                // Điểm đi
                ride.getDeparture(),
                ride.getStartLat(),
                ride.getStartLng(),
                ride.getStartAddress(),
                ride.getStartWard(),
                ride.getStartDistrict(),
                ride.getStartProvince(),

                // Điểm đến
                ride.getEndLat(),
                ride.getEndLng(),
                ride.getEndAddress(),
                ride.getEndWard(),
                ride.getEndDistrict(),
                ride.getEndProvince(),
                ride.getDestination(),

                // Thời gian, giá, ghế, trạng thái
                ride.getStartTime(),
                ride.getPricePerSeat(),
                ride.getTotalSeats(),
                ride.getStatus()
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
                ride.getAvailableSeats(),
                ride.getDriver() != null ? ride.getDriver().getFullName() : null,
                ride.getDriver() != null ? ride.getDriver().getEmail() : null,

                // Điểm đi
                ride.getDeparture(),
                ride.getStartLat(),
                ride.getStartLng(),
                ride.getStartAddress(),
                ride.getStartWard(),
                ride.getStartDistrict(),
                ride.getStartProvince(),

                // Điểm đến
                ride.getEndLat(),
                ride.getEndLng(),
                ride.getEndAddress(),
                ride.getEndWard(),
                ride.getEndDistrict(),
                ride.getEndProvince(),
                ride.getDestination(),

                // Thời gian, giá, ghế, trạng thái
                ride.getStartTime(),
                ride.getPricePerSeat(),
                ride.getTotalSeats(),
                ride.getStatus()
        );

        return dto;
    }

    @Override
    public void updateRide(Long rideId, RideRequestDTO rideRequest, String email) {
        LocalDateTime newStart = rideRequest.getStartTime();
        LocalDateTime newEnd = newStart.plusHours(2); // giả sử chuyến kéo dài 2 giờ

        Rides ride = rideRepository.findDetailRideById(rideId);
        ride.setDeparture(rideRequest.getDeparture());
        ride.setStartLat(rideRequest.getStartLat());
        ride.setStartLng(rideRequest.getStartLng());
        ride.setStartAddress(rideRequest.getStartAddress());
        ride.setStartWard(rideRequest.getStartWard());
        ride.setStartDistrict(rideRequest.getStartDistrict());
        ride.setStartProvince(rideRequest.getStartProvince());
        ride.setEndLat(rideRequest.getEndLat());
        ride.setEndLng(rideRequest.getEndLng());
        ride.setEndAddress(rideRequest.getEndAddress());
        ride.setEndWard(rideRequest.getEndWard());
        ride.setEndDistrict(rideRequest.getEndDistrict());
        ride.setEndProvince(rideRequest.getEndProvince());
        ride.setDestination(rideRequest.getDestination());
        ride.setStartTime(newStart);
        ride.setPricePerSeat(rideRequest.getPricePerSeat());
        ride.setAvailableSeats(rideRequest.getTotalSeat());
        ride.setTotalSeats(rideRequest.getTotalSeat());
        rideRepository.save(ride);

    }

    @Override
    public List<RideRequestDTO> searchRides(String departure, String destination, LocalDate startTime, Integer seats) {
        List<Rides> rides = rideRepository.searchRides(departure, destination, startTime, seats);
        return rides.stream()
                .map(ride -> new RideRequestDTO(
                        ride.getId(),
                        ride.getAvailableSeats(),
                        ride.getDriver() != null ? ride.getDriver().getFullName() : null,
                        ride.getDriver() != null ? ride.getDriver().getEmail() : null,

                        // Điểm đi
                        ride.getDeparture(),
                        ride.getStartLat(),
                        ride.getStartLng(),
                        ride.getStartAddress(),
                        ride.getStartWard(),
                        ride.getStartDistrict(),
                        ride.getStartProvince(),

                        // Điểm đến
                        ride.getEndLat(),
                        ride.getEndLng(),
                        ride.getEndAddress(),
                        ride.getEndWard(),
                        ride.getEndDistrict(),
                        ride.getEndProvince(),
                        ride.getDestination(),

                        // Thời gian, giá, ghế, trạng thái
                        ride.getStartTime(),
                        ride.getPricePerSeat(),
                        ride.getTotalSeats(),
                        ride.getStatus()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<RideRequestDTO> getAllRides() {
        List<Rides> rides = rideRepository.findAll();
        return rides.stream()
                .map(ride -> new RideRequestDTO(
                        ride.getId(),
                        ride.getAvailableSeats(),
                        ride.getDriver() != null ? ride.getDriver().getFullName() : null,
                        ride.getDriver() != null ? ride.getDriver().getEmail() : null,

                        // Điểm đi
                        ride.getDeparture(),
                        ride.getStartLat(),
                        ride.getStartLng(),
                        ride.getStartAddress(),
                        ride.getStartWard(),
                        ride.getStartDistrict(),
                        ride.getStartProvince(),

                        // Điểm đến
                        ride.getEndLat(),
                        ride.getEndLng(),
                        ride.getEndAddress(),
                        ride.getEndWard(),
                        ride.getEndDistrict(),
                        ride.getEndProvince(),
                        ride.getDestination(),

                        // Thời gian, giá, ghế, trạng thái
                        ride.getStartTime(),
                        ride.getPricePerSeat(),
                        ride.getTotalSeats(),
                        ride.getStatus()
                ))
                .collect(Collectors.toList());
    }
}