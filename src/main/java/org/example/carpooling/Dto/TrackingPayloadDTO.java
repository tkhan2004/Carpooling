package org.example.carpooling.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TrackingPayloadDTO {
    private String rideId;
    private String driverEmail;
    private double latitude;
    private double longitude;
    private LocalDateTime timestamp;

    // getters + setters
}
