package org.example.carpooling.Entity;

import jakarta.persistence.*;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Entity.Status.DriverStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ride_id", nullable = false)
    private Rides rides;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private Users passenger;

    private int seatsBooked;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime createdAt;

    // Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rides getRides() {
        return rides;
    }

    public void setRides(Rides rides) {
        this.rides = rides;
    }

    public Users getPassenger() {
        return passenger;
    }

    public void setPassenger(Users passenger) {
        this.passenger = passenger;
    }

    public int getSeatsBooked() {
        return seatsBooked;
    }

    public void setSeatsBooked(int seatsBooked) {
        this.seatsBooked = seatsBooked;
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
}