package org.example.carpooling.Dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.example.carpooling.Entity.Rides;
import org.example.carpooling.Entity.Status.RideStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RideRequestDTO {

    private Long id;
    private int availableSeats;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

//    public RideRequestDTO(Long id, int availableSeats, String driverName, String driverEmail,
//                          String departure, String destination, LocalDateTime startTime,
//                          BigDecimal pricePerSeat, int totalSeat) {
//        this.id = id;
//        this.availableSeats = availableSeats;
//        this.driverName = driverName;
//        this.driverEmail = driverEmail;
//        this.departure = departure;
//        this.destination = destination;
//        this.startTime = startTime;
//        this.pricePerSeat = pricePerSeat;
//        this.totalSeat = totalSeat;
//    }

    private String driverName;

    private String driverEmail;

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    @NotBlank(message=" Điểm khởi hành không được để trống")
    private String departure;

    @NotBlank(message=" Điểm đến không được để trống")
    private String destination;

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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public BigDecimal getPricePerSeat() {
        return pricePerSeat;
    }

    public void setPricePerSeat(BigDecimal pricePerSeat) {
        this.pricePerSeat = pricePerSeat;
    }

    public int getTotalSeat() {
        return totalSeat;
    }

    public void setTotalSeat(int totalSeat) {
        this.totalSeat = totalSeat;
    }

    @NotNull(message = "Thời gian không để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Giá mỗi ghế không được để trống")
    @Min(value = 1000, message = "Giá tối thiểu là 1000VNĐ")
    private BigDecimal pricePerSeat;

    @NotNull(message = "Số ghế không được để trống")
    @Min(value = 1, message = "Tối thiểu là 1 ghế")
    private int totalSeat;

    @Enumerated(EnumType.STRING)
    private RideStatus status = RideStatus.ACTIVE;

    public RideStatus getStatus() {
        return status;
    }

    public RideRequestDTO(Long id, int availableSeats, String driverName, String driverEmail,
                          String departure, String destination, LocalDateTime startTime,
                          BigDecimal pricePerSeat, int totalSeats, RideStatus status) {
        this.id = id;
        this.availableSeats = availableSeats;
        this.driverName = driverName;
        this.driverEmail = driverEmail;
        this.departure = departure;
        this.destination = destination;
        this.startTime = startTime;
        this.pricePerSeat = pricePerSeat;
        this.totalSeat = totalSeats;
        this.status = status;
    }

}
