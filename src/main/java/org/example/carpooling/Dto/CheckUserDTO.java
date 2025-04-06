package org.example.carpooling.Dto;

import org.example.carpooling.Entity.Status.DriverStatus;

//check duyệt tài khoản
public class CheckUserDTO {
    private int driverId;
    private DriverStatus status = DriverStatus.PENDING;
    private String rejectionReason;
    public long getDriverId() {
        return driverId;
    }

    public CheckUserDTO() {
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
