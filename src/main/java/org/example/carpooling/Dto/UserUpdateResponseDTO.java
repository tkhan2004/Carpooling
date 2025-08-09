package org.example.carpooling.Dto;
import lombok.*;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateResponseDTO {

    private String phone;

    private String fullName;

    private String AvatarImageUrl;

    private String licensePlate;

    private String brand;

    private String model;

    private String color;


    private Integer numberOfSeats;

    // Link ảnh (upload Cloudinary hoặc local)
    private String vehicleImageUrl;
    private String licenseImageUrl;

}