package org.example.carpooling.Dto;

import lombok.Data;
import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Status.BookingStatus;

@Data
public class PassengerInfoDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String avatarUrl;
    private BookingStatus status;
    private int seatsBooked;

    public PassengerInfoDTO(Booking booking) {
        this.id = booking.getPassenger().getId();
        this.name = booking.getPassenger().getFullName();
        this.phone = booking.getPassenger().getPhone();
        this.email = booking.getPassenger().getEmail();
        this.avatarUrl = booking.getPassenger().getAvatarImage();
        this.status = booking.getStatus();
        this.seatsBooked = booking.getSeatsBooked();
    }

    public PassengerInfoDTO() {}
}
