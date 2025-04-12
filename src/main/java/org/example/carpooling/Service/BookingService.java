package org.example.carpooling.Service;

import org.example.carpooling.Dto.BookingDTO;

import java.util.List;

public interface BookingService {
    public BookingDTO bookRide(Long rideId, int seats, String email);
    public BookingDTO passengerConfirm(Long rideId, String email);
    public void driverMarkCompleted(Long rideId );
    void driverAcceptBooking(Long bookingId);
    public List<BookingDTO> getBookingsForDriver(String driverEmail);
}
