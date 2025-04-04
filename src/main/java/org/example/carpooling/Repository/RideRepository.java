package org.example.carpooling.Repository;

import org.example.carpooling.Dto.RideRequestDTO;
import org.example.carpooling.Entity.Rides;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface RideRepository extends JpaRepository<Rides, Long> {
    List<Rides> findByDriver_Email(String email);
    List<Rides> findByStatus(String status);

    @Query("SELECT new org.example.carpooling.Dto.RideRequestDTO(" +
            "r.id, r.available_seats, r.driver.fullName, r.driver.email, " +
            "r.departure, r.destination, r.start_time, r.price_per_seat, r.total_seats, r.status) " +
            "FROM Rides r WHERE r.status = 'ACTIVE'")
    List<RideRequestDTO> findAllRidesByStatus();

    Rides findDetailRideById(Long rideId);

    Rides findCancelRideById(Long rideId);
}