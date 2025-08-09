package org.example.carpooling.Dto;

import jakarta.validation.constraints.Email;
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
public class RegisterRequest {

    // ---------------------- USER INFO ----------------------
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotNull(message = "Vai trò (roleId) không được để trống")
    private Long roleId;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    private String fullName;


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
    private String vehicleImageUrl;
    private String licenseImageUrl;
}
