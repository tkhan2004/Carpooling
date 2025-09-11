package org.example.carpooling.Service.Imp;

import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Entity.Status.RideStatus;
import org.example.carpooling.Repository.BookingRepository;
import org.example.carpooling.Repository.RideRepository;
import org.example.carpooling.Service.RideStatusScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RideStatusSchedulerImp implements RideStatusScheduler {

    @Autowired
    RideRepository rideRepository;
    
    @Autowired
    BookingRepository bookingRepository;

    @Override
    @Scheduled(fixedRate = 30000)
    public void updateOngoingRides() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findBookingByStatus(BookingStatus.ACCEPTED);

        for ( Booking booking : bookings ) {
            if (booking.getRides().getStartTime().isBefore(now)){
                booking.setStatus(BookingStatus.IN_PROGRESS);
            }
        }
        bookingRepository.saveAll(bookings);
        List<Rides> rides = rideRepository.findRidesByStatus(RideStatus.ACTIVE);

        for ( Rides ride : rides ) {
            if (ride.getStartTime().isBefore(now)){
                if (ride.getBookings() == null || ride.getBookings().isEmpty()) {
                    ride.setStatus(RideStatus.COMPLETED);
                }else {
                    ride.setStatus(RideStatus.IN_PROGRESS);
                }
            }
        }
        rideRepository.saveAll(rides);
    }
}
