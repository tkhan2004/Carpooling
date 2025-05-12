package org.example.carpooling.Entity.Status;

public enum RideStatus {
    ACTIVE,             // Chờ đến giờ bắt đầu
    IN_PROGRESS,        // Đã tới giờ bắt đầu → đang diễn ra
    DRIVER_CONFIRMED,   // Tài xế xác nhận hoàn thành
    COMPLETED,          // Cả tài xế + hành khách đã xác nhận
    CANCELLED           // Bị hủy
}
