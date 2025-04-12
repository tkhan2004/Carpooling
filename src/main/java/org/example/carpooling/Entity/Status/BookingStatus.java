package org.example.carpooling.Entity.Status;

public enum BookingStatus {
        PENDING, // Chờ duyệt
        ACCEPTED, // Được duyệt (chờ đi)
        PASSENGER_CONFIRMED, // Khách xác nhận hoàn thành
        REJECTED, // Bị từ chối
        CANCELLED // Khách huỷ
}