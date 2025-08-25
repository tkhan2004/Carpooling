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
}
