package org.example.carpooling.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleDTO {
    private String licensePlate;
    private String brand;
    private String model;
    private String color;
    private Integer numberOfSeats;
    private String vehicleImageUrl;
    private String licenseImageUrl;
    private String licenseImagePublicId;
    private String vehicleImagePublicId;

    public VehicleDTO(String licensePlate, String brand, String model, String color, Integer numberOfSeats, String vehicleImageUrl) {
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.numberOfSeats = numberOfSeats;
        this.vehicleImageUrl = vehicleImageUrl;
    }

}
