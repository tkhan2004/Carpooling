package org.example.carpooling.Entity;

import jakarta.persistence.*;

@Entity
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licensePlate;
    private String brand;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(Integer numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public String getVehicleImageUrl() {
        return vehicleImageUrl;
    }

    public void setVehicleImageUrl(String vehicleImageUrl) {
        this.vehicleImageUrl = vehicleImageUrl;
    }

    public String getLicenseImageUrl() {
        return licenseImageUrl;
    }

    public void setLicenseImageUrl(String licenseImageUrl) {
        this.licenseImageUrl = licenseImageUrl;
    }

    public Users getDriver() {
        return driver;
    }

    public void setDriver(Users driver) {
        this.driver = driver;
    }

    private String model;
    private String color;
    private Integer numberOfSeats;
    private String vehicleImageUrl;
    private String licenseImageUrl;
    private String licenseImagePublicId;
    private String vehicleImagePublicId;

    public String getLicenseImagePublicId() {
        return licenseImagePublicId;
    }

    public void setLicenseImagePublicId(String licenseImagePublicId) {
        this.licenseImagePublicId = licenseImagePublicId;
    }

    public String getVehicleImagePublicId() {
        return vehicleImagePublicId;
    }

    public void setVehicleImagePublicId(String vehicleImagePublicId) {
        this.vehicleImagePublicId = vehicleImagePublicId;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users driver;
}
