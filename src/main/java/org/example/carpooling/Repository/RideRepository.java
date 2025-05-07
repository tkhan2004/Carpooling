package org.example.carpooling.Repository;

import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface RideRepository extends JpaRepository<Rides, Long> {
    List<Rides> findByDriver_Email(String email);

    @Query("SELECT new org.example.carpooling.Dto.RideRequestDTO(" +
            "r.id, r.available_seats, r.driver.fullName, r.driver.email, " +
            "r.departure, r.destination, r.start_time, r.price_per_seat, r.total_seats, r.status) " +
            "FROM Rides r WHERE r.status = 'ACTIVE'")
    List<RideRequestDTO> findAllRidesByStatus();

    Rides findDetailRideById(Long rideId);

    Rides findCancelRideById(Long rideId);

    Rides findRideById(Long rideId);

    @Query("SELECT r FROM Rides r WHERE " +
            "(:departure IS NULL OR LOWER(r.departure) LIKE LOWER(CONCAT('%', :departure, '%'))) AND " +
            "(:destination IS NULL OR LOWER(r.destination) LIKE LOWER(CONCAT('%', :destination, '%'))) AND " +
            "(:startTime IS NULL OR DATE(r.start_time) = :startTime) AND " +
            "(:seats IS NULL OR r.available_seats >= :seats) AND " +
            "r.status = 'ACTIVE'")
    List<Rides> searchRides(
            @Param("departure") String departure,
            @Param("destination") String destination,
            @Param("startTime") LocalDate startTime,
            @Param("seats") Integer seats);

}