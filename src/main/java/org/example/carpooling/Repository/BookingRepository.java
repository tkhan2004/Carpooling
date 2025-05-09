package org.example.carpooling.Repository;

import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Entity.Users;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface
BookingRepository extends JpaRepository<Booking,Long> {

    List<Booking> findByRides_IdAndPassenger_Id(Long rideId, Long passengerId);
    List<Booking> findAllByPassenger(Users passenger);
    List<Booking> findAllByRidesIn(List<Rides> rides);
    
    // Tìm booking theo ride ID và email của hành khách
    Optional<Booking> findByRidesIdAndPassengerEmail(Long rideId, String passengerEmail);
    
    // Tìm tất cả booking của một chuyến đi
    List<Booking> findByRidesId(Long rideId);

    boolean existsByPassengerAndRidesAndStatusIn(Users passenger, Rides ride, List<BookingStatus> statuses);
}
