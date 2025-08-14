package org.example.carpooling.Dto.Request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.example.carpooling.Entity.Status.RideStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestDTO {

    private Long id;
    private int availableSeats;

    private String driverName;

    private String driverEmail;

    // ====== Điểm đi ======
    @NotBlank(message=" Điểm khởi hành không được để trống")
    private String departure;

    @NotBlank(message=" Điểm khởi hành không được để trống")
    private Double startLat;

    @NotBlank(message=" Điểm khởi hành không được để trống")
    private Double startLng;

    @NotBlank(message=" Điểm khởi hành không được để trống")
    private String startAddress;

    @NotBlank(message=" Điểm khởi hành không được để trống")
    private String startWard;

    @NotBlank(message=" Điểm khởi hành không được để trống")
    private String startDistrict;

    @NotBlank(message=" Điểm khởi hành không được để trống")
    private String startProvince;

    // ====== Điểm đến ======
    @NotBlank(message=" Điểm đến không được để trống")
    private Double endLat;

    @NotBlank(message=" Điểm đến không được để trống")
    private Double endLng;

    @NotBlank(message=" Điểm đến không được để trống")
    private String endAddress;

    @NotBlank(message=" Điểm đến không được để trống")
    private String endWard;

    @NotBlank(message=" Điểm đến không được để trống")
    private String endDistrict;

    @NotBlank(message=" Điểm đến không được để trống")
    private String endProvince;

    @NotBlank(message=" Điểm đến không được để trống")
    private String destination;



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


}
