package org.example.carpooling.Dto.Response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.carpooling.Dto.VehicleDTO;
import org.example.carpooling.Entity.Status.RideStatus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RideResponseDTO {
    private Long id;
    private int availableSeats;

    private String driverName;

    private String driverEmail;

    VehicleDTO vehicle;

    // ====== Điểm đi ======
    private String departure;

    private Double startLat;

    private Double startLng;

    private String startAddress;

    private String startWard;

    private String startDistrict;

    private String startProvince;

    // ====== Điểm đến ======
    private Double endLat;

    private Double endLng;

    private String endAddress;

    private String endWard;

    private String endDistrict;

    private String endProvince;

    private String destination;

    private LocalDateTime startTime;

    private BigDecimal pricePerSeat;

    private int totalSeat;

    @Enumerated(EnumType.STRING)
    private RideStatus status = RideStatus.ACTIVE;

}
