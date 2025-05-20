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
    List<Booking> findBookingByStatus(BookingStatus status);
    List<Booking> findAllByRidesIn(List<Rides> rides);
    
    // Tìm booking theo ride ID và email của hành khách
    List<Booking> findByRides_IdAndPassenger_Email(Long rideId, String email);
    // Tìm tất cả booking của một chuyến đi
    List<Booking> findByRidesId(Long rideId);

    boolean existsByPassengerAndRidesAndStatusIn(Users passenger, Rides ride, List<BookingStatus> statuses);

    List<Booking> findByPassenger_Email(String passengerEmail);

    @Query("SELECT b FROM Booking b WHERE b.passenger.email = :email")
    List<Booking> findBookingsByPassengerEmail(@Param("email") String email);

    List<Booking> findByRidesIdAndStatusIn(Long id, List<BookingStatus> statuses);
    @Query("SELECT b FROM Booking b WHERE b.rides.id = :rideId AND b.passenger.id = :passengerId AND b.status NOT IN :statuses ORDER BY b.createdAt DESC")
    List<Booking> findByRides_IdAndPassenger_IdAndStatusNotInOrderByCreatedAtDesc(
            @Param("rideId") Long rideId,
            @Param("passengerId") Long passengerId,
            @Param("statuses") List<BookingStatus> statuses
    );

}
