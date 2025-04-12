package org.example.carpooling.Dto;

import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Status.BookingStatus;

import java.time.LocalDateTime;

public class BookingDTO {
    private Long id;
    private Long rideId;
    private Long passengerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public void setSeatsBooked(int seatsBooked) {
        this.seatsBooked = seatsBooked;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public BookingDTO(Booking booking) {
        this.id = booking.getId();
        this.rideId = booking.getRides().getId();
        this.passengerId = booking.getPassenger().getId();
        this.passengerName = booking.getPassenger().getFullName();
        this.seatsBooked = booking.getSeatsBooked();
        this.status = booking.getStatus();
        this.createdAt = booking.getCreatedAt();
    }
    public BookingDTO(Long id, Long rideId, Long passengerId, int seatsBooked, String passengerName, BookingStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.seatsBooked = seatsBooked;
        this.passengerName = passengerName;
        this.status = status;
        this.createdAt = createdAt;
    }

    private int seatsBooked;
    private String passengerName; // tuỳ bạn muốn show gì thêm
    private BookingStatus status;
    private LocalDateTime createdAt;
}
