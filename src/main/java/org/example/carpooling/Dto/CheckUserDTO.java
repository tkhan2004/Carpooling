package org.example.carpooling.Dto;

import lombok.*;
import org.example.carpooling.Entity.Status.DriverStatus;

//check duyệt tài khoản
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckUserDTO {
    private int driverId;
    private DriverStatus status = DriverStatus.PENDING;
    private String rejectionReason;
}
