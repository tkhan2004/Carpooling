package org.example.carpooling.Service;

import org.example.carpooling.Dto.BookingDTO;
import org.example.carpooling.Entity.Status.BookingStatus;

import java.util.List;

public interface BookingService {
    public BookingDTO bookRide(Long rideId, int seats, String email);
    public BookingDTO passengerConfirm(Long rideId, String email);
    public void driverMarkCompleted(Long rideId);
    void driverAcceptBooking(Long bookingId);
    public List<BookingDTO> getBookingsForDriver(String driverEmail);
    public List<BookingDTO> getBookingsForPassenger(String passengerEmail);
    public BookingDTO cancleBookings(Long rideId, String email);
    
    // Thêm 2 phương thức mới
    public List<BookingDTO> getBookingsForPassengerByStatus(String passengerEmail, List<BookingStatus> statuses);
    public List<BookingDTO> getBookingsForDriverByStatus(String driverEmail, List<BookingStatus> statuses);

}
