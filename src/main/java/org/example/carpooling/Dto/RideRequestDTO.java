package org.example.carpooling.Dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.example.carpooling.Entity.Rides;
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


    @NotBlank(message=" Điểm khởi hành không được để trống")
    private String departure;

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
