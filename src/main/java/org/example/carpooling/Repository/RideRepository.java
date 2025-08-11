package org.example.carpooling.Repository;

import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Status.RideStatus;
import org.example.carpooling.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface RideRepository extends JpaRepository<Rides, Long> {

    List<Rides> findByDriver_Email(String email);

    @Query("SELECT new org.example.carpooling.Dto.RideRequestDTO(" +
            "r.id, r.availableSeats, r.driver.fullName, r.driver.email, " +
            "r.departure, r.startLat, r.startLng, r.startAddress, r.startWard, r.startDistrict, r.startProvince, " +
            "r.endLat, r.endLng, r.endAddress, r.endWard, r.endDistrict, r.endProvince, r.destination, " +
            "r.startTime, r.pricePerSeat, r.totalSeats, r.status) " +
            "FROM Rides r WHERE r.status = 'ACTIVE'")
    List<RideRequestDTO> findAllRidesByStatus();

    Rides findDetailRideById(Long rideId);

    Rides findCancelRideById(Long rideId);

    Rides findRideById(Long rideId);

    @Query("SELECT r FROM Rides r WHERE " +
            "(:departure IS NULL OR LOWER(r.departure) LIKE LOWER(CONCAT('%', :departure, '%'))) AND " +
            "(:destination IS NULL OR LOWER(r.destination) LIKE LOWER(CONCAT('%', :destination, '%'))) AND " +
            "(:startTime IS NULL OR DATE(r.startTime) = :startTime) AND " +
            "(:seats IS NULL OR r.availableSeats >= :seats) AND " +  // ✅ sửa chỗ này
            "r.status = 'ACTIVE'")
    List<Rides> searchRides(
            @Param("departure") String departure,
            @Param("destination") String destination,
            @Param("startTime") LocalDate startTime,
            @Param("seats") Integer seats);

    List<Rides> findByDriverAndStatus(Users driver, RideStatus status);
    List<Rides> findRidesByStatus(RideStatus status);
}