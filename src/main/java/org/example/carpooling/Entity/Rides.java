package org.example.carpooling.Entity;

import jakarta.persistence.*;
import org.example.carpooling.Entity.Status.RideStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="rides")
public class Rides {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="driver_id" , nullable = false)
    private Users driver;

    private String departure;
    private String destination;

    public LocalDateTime getStart_time() {
        return start_time;
    }

    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getDriver() {
        return driver;
    }

    public void setDriver(Users driver) {
        this.driver = driver;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public BigDecimal getPrice_per_seat() {
        return price_per_seat;
    }

    public void setPrice_per_seat(BigDecimal price_per_seat) {
        this.price_per_seat = price_per_seat;
    }

    public int getTotal_seats() {
        return total_seats;
    }

    public void setTotal_seats(int total_seats) {
        this.total_seats = total_seats;
    }

    public int getAvailable_seats() {
        return available_seats;
    }

    public void setAvailable_seats(int available_seats) {
        this.available_seats = available_seats;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    private LocalDateTime start_time;

    private BigDecimal price_per_seat;
    private int total_seats;
    private int available_seats;

    @Enumerated(EnumType.STRING)
    private RideStatus status = RideStatus.ACTIVE;
    private LocalDateTime created_at = LocalDateTime.now();

    @OneToMany(mappedBy = "rides")
    private List<Booking> bookings = new ArrayList<>();

}
