package org.example.carpooling.Dto.Request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDTO {
    // ---------------------- USER INFO ----------------------

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    private String fullName;

    private MultipartFile AvatarImageUrl;


    // ---------------------- VEHICLE INFO ----------------------
    @NotBlank(message = "Biển số xe không được để trống")
    @Pattern(
            regexp = "^[0-9]{2}[A-Z]{1,2}-[0-9]{4,6}$",
            message = "Biển số xe không hợp lệ (VD: 51F-12345)"
    )
    private String licensePlate;

    @NotBlank(message = "Hãng xe không được để trống")
    private String brand;

    @NotBlank(message = "Mẫu xe không được để trống")
    private String model;

    @NotBlank(message = "Màu xe không được để trống")
    private String color;

    @NotNull(message = "Số ghế không được để trống")
    @Min(value = 1, message = "Số ghế tối thiểu là 1")
    private Integer numberOfSeats;

    // Link ảnh (upload Cloudinary hoặc local)
    private MultipartFile vehicleImageUrl;
    private MultipartFile licenseImageUrl;

    // Getter và Setter
}
