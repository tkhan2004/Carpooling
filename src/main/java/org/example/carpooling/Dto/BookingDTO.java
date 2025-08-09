package org.example.carpooling.Dto;

import lombok.*;
import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Entity.Vehicle;
import org.example.carpooling.Service.FileService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BookingDTO {
    // Thông tin đặt chỗ
    private Long id;
    private Long rideId;
    private int seatsBooked;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice; // Giá tổng = seats * pricePerSeat

    // Thông tin chuyến đi
    private String departure;
    private String destination;
    private LocalDateTime startTime;
    private BigDecimal pricePerSeat;
    private String rideStatus;
    private int totalSeats;
    private int availableSeats;

    // Thông tin tài xế
    private Long driverId;
    private String driverName;
    private String driverPhone;
    private String driverEmail;
    private String driverAvatarUrl;
    private String driverVehicleImageUrl;
    private String driverLicenseImageUrl;
    private String driverStatus;

    // Thông tin hành khách
    private Long passengerId;
    private String passengerName;
    private String passengerPhone;
    private String passengerEmail;
    private String passengerAvatarUrl;

    // Thông tin các hành khách cùng chuyến
    private List<PassengerInfo> fellowPassengers;

    // Lớp để lưu thông tin về các hành khách cùng chuyến
    public static class PassengerInfo {
        private Long id;
        private String name;
        private String phone;
        private String email;
        private String avatarUrl;
        private BookingStatus status;
        private int seatsBooked;

        public PassengerInfo(Booking booking, FileService fileService) {
            this.id = booking.getPassenger().getId();
            this.name = booking.getPassenger().getFullName();
            this.phone = booking.getPassenger().getPhone();
            this.email = booking.getPassenger().getEmail();
            this.avatarUrl = fileService.generateFileUrl(booking.getPassenger().getAvatarImage());
            this.status = booking.getStatus();
            this.seatsBooked = booking.getSeatsBooked();
        }

        // Constructor mặc định
        public PassengerInfo() {
        }

    }

    // Constructor mặc định
    public BookingDTO() {
    }

    // Constructor chính
    public BookingDTO(Booking booking ) {
        // Thông tin đặt chỗ
        this.id = booking.getId();
        this.rideId = booking.getRides().getId();
        this.seatsBooked = booking.getSeatsBooked();
        this.status = booking.getStatus();
        this.createdAt = booking.getCreatedAt();

        // Thông tin chuyến đi
        this.departure = booking.getRides().getDeparture();
        this.destination = booking.getRides().getDestination();
        this.startTime = booking.getRides().getStartTime();
        this.pricePerSeat = booking.getRides().getPricePerSeat();
        this.rideStatus = booking.getRides().getStatus().toString();
        this.totalSeats = booking.getRides().getTotalSeats();
        this.availableSeats = booking.getRides().getAvailableSeats();

        // Tính tổng tiền
        if (this.pricePerSeat != null) {
            this.totalPrice = this.pricePerSeat.multiply(BigDecimal.valueOf(this.seatsBooked));
        }

        // Thông tin tài xế
        if (booking.getRides().getDriver() != null) {
            Vehicle vehicle = booking.getRides().getDriver().getVehicles().get(0);
            this.driverId = booking.getRides().getDriver().getId();
            this.driverName = booking.getRides().getDriver().getFullName();
            this.driverPhone = booking.getRides().getDriver().getPhone();
            this.driverEmail = booking.getRides().getDriver().getEmail();
            this.driverStatus = booking.getRides().getDriver().getStatus().toString();
            this.driverAvatarUrl = booking.getRides().getDriver().getAvatarImage();
            this.driverVehicleImageUrl = vehicle.getVehicleImageUrl();
            this.driverLicenseImageUrl = vehicle.getLicenseImageUrl();
        }

        // Thông tin hành khách
        if (booking.getPassenger() != null) {
            this.passengerId = booking.getPassenger().getId();
            this.passengerName = booking.getPassenger().getFullName();
            this.passengerPhone = booking.getPassenger().getPhone();
            this.passengerEmail = booking.getPassenger().getEmail();
            this.passengerAvatarUrl = booking.getPassenger().getAvatarImage();
        }
    }

    // Constructor phụ - giữ lại để tương thích ngược
    public BookingDTO(Long id, Long rideId, Long passengerId, int seatsBooked, String passengerName, BookingStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.seatsBooked = seatsBooked;
        this.passengerName = passengerName;
        this.status = status;
        this.createdAt = createdAt;
    }

}