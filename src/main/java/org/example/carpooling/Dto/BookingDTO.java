package org.example.carpooling.Dto;

import org.example.carpooling.Entity.Booking;
import org.example.carpooling.Entity.Status.BookingStatus;
import org.example.carpooling.Service.FileService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

        // Getters và setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public BookingStatus getStatus() {
            return status;
        }

        public void setStatus(BookingStatus status) {
            this.status = status;
        }

        public int getSeatsBooked() {
            return seatsBooked;
        }

        public void setSeatsBooked(int seatsBooked) {
            this.seatsBooked = seatsBooked;
        }
    }

    // Constructor mặc định
    public BookingDTO() {
    }

    // Constructor chính
    public BookingDTO(Booking booking, FileService fileService) {
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
            this.driverId = booking.getRides().getDriver().getId();
            this.driverName = booking.getRides().getDriver().getFullName();
            this.driverPhone = booking.getRides().getDriver().getPhone();
            this.driverEmail = booking.getRides().getDriver().getEmail();
            this.driverStatus = booking.getRides().getDriver().getStatus().toString();
            this.driverAvatarUrl = fileService.generateFileUrl(booking.getRides().getDriver().getAvatarImage());
            this.driverVehicleImageUrl = fileService.generateFileUrl(booking.getRides().getDriver().getVehicleImageUrl());
            this.driverLicenseImageUrl = fileService.generateFileUrl(booking.getRides().getDriver().getLicenseImageUrl());
        }

        // Thông tin hành khách
        if (booking.getPassenger() != null) {
            this.passengerId = booking.getPassenger().getId();
            this.passengerName = booking.getPassenger().getFullName();
            this.passengerPhone = booking.getPassenger().getPhone();
            this.passengerEmail = booking.getPassenger().getEmail();
            this.passengerAvatarUrl = fileService.generateFileUrl(booking.getPassenger().getAvatarImage());
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

    // Getters và setters
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
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

    public String getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String rideStatus) {
        this.rideStatus = rideStatus;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getDriverEmail() {
        return driverEmail;
    }

    public void setDriverEmail(String driverEmail) {
        this.driverEmail = driverEmail;
    }

    public String getDriverAvatarUrl() {
        return driverAvatarUrl;
    }

    public void setDriverAvatarUrl(String driverAvatarUrl) {
        this.driverAvatarUrl = driverAvatarUrl;
    }

    public String getDriverVehicleImageUrl() {
        return driverVehicleImageUrl;
    }

    public void setDriverVehicleImageUrl(String driverVehicleImageUrl) {
        this.driverVehicleImageUrl = driverVehicleImageUrl;
    }

    public String getDriverLicenseImageUrl() {
        return driverLicenseImageUrl;
    }

    public void setDriverLicenseImageUrl(String driverLicenseImageUrl) {
        this.driverLicenseImageUrl = driverLicenseImageUrl;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getPassengerAvatarUrl() {
        return passengerAvatarUrl;
    }

    public void setPassengerAvatarUrl(String passengerAvatarUrl) {
        this.passengerAvatarUrl = passengerAvatarUrl;
    }

    public List<PassengerInfo> getFellowPassengers() {
        return fellowPassengers;
    }

    public void setFellowPassengers(List<PassengerInfo> fellowPassengers) {
        this.fellowPassengers = fellowPassengers;
    }
}