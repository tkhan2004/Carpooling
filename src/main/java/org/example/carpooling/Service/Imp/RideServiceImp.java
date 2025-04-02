package org.example.carpooling.Service.Imp;

import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Users;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Repository.UserRepository;
import org.example.carpooling.Service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RideServiceImp implements RideService {

    private final UserRepository userRepository;
    private final RideRepository rideRepository;

    @Autowired
    public RideServiceImp(UserRepository userRepository, RideRepository rideRepository) {
        this.userRepository = userRepository;
        this.rideRepository = rideRepository;
    }

    @Override
    public ResponseEntity<?> createRide(RideRequestDTO rideRequest, String email) {
        // Kiểm tra tài xế có tồn tại không
        Optional<Users> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy tài xế");
        }

        Users driver = optionalUser.get();
        if (!driver.getRole().getName().equals("DRIVER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Chỉ tài xế mới có thể tạo chuyến đi");
        }

        // Tạo chuyến đi mới
        Rides ride = new Rides();
        ride.setDriver(driver);
        ride.setDeparture(rideRequest.getDeparture());
        ride.setDestination(rideRequest.getDestination());
        ride.setStart_time(rideRequest.getStartTime());
        ride.setPrice_per_seat(rideRequest.getPricePerSeat());
        ride.setAvailable_seats(rideRequest.getTotalSeat());
        ride.setTotal_seats(rideRequest.getTotalSeat());

        // Lưu vào database
        rideRepository.save(ride);

        return ResponseEntity.status(HttpStatus.CREATED).body("Tạo chuyến đi thành công");
    }


    @Override
    public List<RideRequestDTO> getAllRideActive() {
        return rideRepository.findAllRidesByStatus("ACTIVE");
    }

}

