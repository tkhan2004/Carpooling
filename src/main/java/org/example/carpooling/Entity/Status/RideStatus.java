package org.example.carpooling.Entity.Status;

public enum RideStatus {
    ACTIVE, // Đang chờ thực hiện
    DRIVER_CONFIRMED, // Tài xế xác nhận xong
    COMPLETED, // Hệ thống tự set nếu cả 2 bên đều xác nhận
    CANCELLED
}
