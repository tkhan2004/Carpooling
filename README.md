# 🚗 Carpooling System
Carpooling là một hệ thống giúp người có xe chia sẻ chỗ trống trên xe với những người có cùng tuyến đường, giúp tiết kiệm chi phí và bảo vệ môi trường.
## 🌟 Tính năng chính

### 👤 Quản trị
- Quản lý xét duyệt tài xế.
- Quản lý tài khoản.
- Theo dõi chuyến đi. ( sẽ phát triển )

### 👤 Người dùng
- Đăng ký / Đăng nhập (Email + JWT Authentication)
- Cập nhật thông tin cá nhân
- Đổi mật khẩu
- Xem thông tin cá nhân
- Quản lý đặt xe.

### 🚕 Tài xế
- Đăng ký tài khoản với vai trò tài xế
- Tải ảnh:
  - Ảnh đại diện (avatar)
  - Giấy phép lái xe
  - Ảnh xe
- Quản lý thông tin tài xế
- Tạo chuyến đi, quản lý chuyến đi

### 🔐 Phân quyền
- Sử dụng `Role` để phân biệt tài xế và khách
- Bảo vệ endpoint bằng Spring Security + `@PreAuthorize`

### 🧾 API Authentication
- Login trả về JWT token riêng cho từng người dùng
- Token dùng để xác thực cho các request tiếp theo

### Sẽ phát triển trong tương lai
- Websocket : real-time, chat trao đổi giữa tài xế và khách hàng.
- Tích hợp GG-Map API để sử dụng gps định vị tài xế và khách hàng.

---

## ⚙️ Công nghệ sử dụng

- 🔙 Backend: Java Spring Boot
- 🛡️ Bảo mật: Spring Security + JWT
- 💾 Database: MySQL
- 📂 Upload ảnh: Multipart File + lưu thư mục máy local
- 🛠️ Maven (Quản lý dependency)


# API Documentation - Carpooling Application

## Tổng quan

API này cung cấp các chức năng cho ứng dụng đi chung xe (carpooling) với các vai trò chính:
- **Hành khách** (PASSENGER): Người cần tìm và đặt chuyến đi
- **Tài xế** (DRIVER): Người cung cấp dịch vụ chở khách
- **Quản trị viên** (ADMIN): Người quản lý hệ thống

## Xác thực

### Đăng ký

#### Đăng ký tài khoản hành khách
```
POST /api/auth/passenger-register
Content-Type: multipart/form-data
```

**Parameters:**
- `email`: Email của người dùng
- `password`: Mật khẩu
- `fullName`: Họ và tên
- `phone`: Số điện thoại
- `avatarImage` (optional): File hình ảnh đại diện

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "example@gmail.com",
    "phone": "0123456789",
    "avatarUrl": "http://example.com/images/avatar.jpg",
    "role": "PASSENGER"
  }
}
```

#### Đăng ký tài khoản tài xế
```
POST /api/auth/driver-register
Content-Type: multipart/form-data
```

**Parameters:**
- `email`: Email của tài xế
- `password`: Mật khẩu
- `fullName`: Họ và tên
- `phone`: Số điện thoại
- `avatarImage` (optional): File hình ảnh đại diện
- `licenseImage` (optional): File hình ảnh giấy phép lái xe
- `vehicleImage` (optional): File hình ảnh phương tiện

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký thành công",
  "data": {
    "id": 2,
    "status": "PENDING",
    "licenseImageUrl": "http://example.com/images/license.jpg",
    "vehicleImageUrl": "http://example.com/images/vehicle.jpg",
    "avatarUrl": "http://example.com/images/avatar.jpg",
    "fullName": "Nguyễn Văn B",
    "email": "driver@gmail.com",
    "phone": "0987654321",
    "role": "DRIVER"
  }
}
```

### Đăng nhập
```
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "example@gmail.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "email": "example@gmail.com",
    "role": "PASSENGER"
  }
}
```

## Quản lý chuyến đi

### Tìm kiếm chuyến đi
```
GET /api/ride/search
Authorization: Bearer {token}
```

**Parameters:**
- `departure` (optional): Điểm xuất phát
- `destination` (optional): Điểm đến
- `startTime` (optional): Thời gian khởi hành (định dạng ISO: YYYY-MM-DD)
- `seats` (optional): Số ghế cần đặt

**Response:**
```json
{
  "success": true,
  "message": "Tìm chuyến đi thành công",
  "data": [
    {
      "id": 1,
      "departure": "Hà Nội",
      "destination": "Hải Phòng",
      "startTime": "2023-11-01T08:00:00",
      "availableSeats": 3,
      "price": 150000,
      "status": "ACTIVE",
      "driver": {
        "id": 2,
        "fullName": "Nguyễn Văn B",
        "avatarUrl": "http://example.com/images/avatar.jpg",
        "phone": "0987654321"
      }
    }
  ]
}
```

### Xem danh sách chuyến đi đang hoạt động
```
GET /api/ride/available
```

**Response:**
```json
{
  "success": true,
  "message": "success",
  "data": [
    {
      "id": 1,
      "departure": "Hà Nội",
      "destination": "Hải Phòng",
      "startTime": "2023-11-01T08:00:00",
      "availableSeats": 3,
      "price": 150000,
      "status": "ACTIVE",
      "driver": {
        "id": 2,
        "fullName": "Nguyễn Văn B",
        "avatarUrl": "http://example.com/images/avatar.jpg",
        "phone": "0987654321"
      }
    }
  ]
}
```

### Tạo chuyến đi mới (chỉ tài xế)
```
POST /api/ride
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "departure": "Hà Nội",
  "destination": "Hải Phòng",
  "startTime": "2023-11-01T08:00:00",
  "availableSeats": 4,
  "price": 150000
}
```

**Response:**
```json
{
  "success": true,
  "message": "Tạo chuyến đi thành công",
  "data": null
}
```

### Xem chi tiết chuyến đi
```
GET /api/ride/{id}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách chuyến đi",
  "data": {
    "id": 1,
    "departure": "Hà Nội",
    "destination": "Hải Phòng",
    "startTime": "2023-11-01T08:00:00",
    "availableSeats": 3,
    "price": 150000,
    "status": "ACTIVE",
    "driver": {
      "id": 2,
      "fullName": "Nguyễn Văn B",
      "avatarUrl": "http://example.com/images/avatar.jpg",
      "phone": "0987654321"
    }
  }
}
```

### Hủy chuyến đi (chỉ tài xế)
```
PUT /api/ride/cancel/{id}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Huỷ bỏ chuyến đi thành công",
  "data": {
    "id": 1,
    "departure": "Hà Nội",
    "destination": "Hải Phòng",
    "startTime": "2023-11-01T08:00:00",
    "availableSeats": 3,
    "price": 150000,
    "status": "CANCELLED",
    "driver": {
      "id": 2,
      "fullName": "Nguyễn Văn B",
      "avatarUrl": "http://example.com/images/avatar.jpg",
      "phone": "0987654321"
    }
  }
}
```

### Cập nhật chuyến đi (chỉ tài xế)
```
PUT /api/ride/update/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "departure": "Hà Nội",
  "destination": "Hải Phòng",
  "startTime": "2023-11-01T09:00:00",
  "availableSeats": 3,
  "price": 180000
}
```

**Response:**
```json
{
  "success": true,
  "message": "Cập nhật chuyến đi thành công",
  "data": {
    "id": 1,
    "departure": "Hà Nội",
    "destination": "Hải Phòng",
    "startTime": "2023-11-01T09:00:00",
    "availableSeats": 3,
    "price": 180000,
    "status": "ACTIVE",
    "driver": {
      "id": 2,
      "fullName": "Nguyễn Văn B",
      "avatarUrl": "http://example.com/images/avatar.jpg",
      "phone": "0987654321"
    }
  }
}
```

## Quản lý đặt chỗ (Booking)

### Đặt chỗ (chỉ hành khách)
```
POST /api/passenger/booking/{rideId}
Authorization: Bearer {token}
```

**Parameters:**
- `seats`: Số ghế cần đặt

**Response:**
```json
{
  "success": true,
  "message": "Đăng ký chuyến đi thành công",
  "data": {
    "id": 1,
    "rideId": 1,
    "passengerId": 1,
    "passengerName": "Nguyễn Văn A",
    "driverId": 2,
    "driverName": "Nguyễn Văn B",
    "departure": "Hà Nội",
    "destination": "Hải Phòng",
    "bookingTime": "2023-10-30T15:30:00",
    "startTime": "2023-11-01T08:00:00",
    "seats": 2,
    "price": 300000,
    "status": "PENDING"
  }
}
```

### Xem danh sách đặt chỗ (hành khách)
```
GET /api/passenger/bookings
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách bookings của khách hàng",
  "data": [
    {
      "id": 1,
      "rideId": 1,
      "passengerId": 1,
      "passengerName": "Nguyễn Văn A",
      "driverId": 2,
      "driverName": "Nguyễn Văn B",
      "departure": "Hà Nội",
      "destination": "Hải Phòng",
      "bookingTime": "2023-10-30T15:30:00",
      "startTime": "2023-11-01T08:00:00",
      "seats": 2,
      "price": 300000,
      "status": "PENDING"
    }
  ]
}
```

### Xem danh sách đặt chỗ (tài xế)
```
GET /api/driver/bookings
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách bookings của tài xế",
  "data": [
    {
      "id": 1,
      "rideId": 1,
      "passengerId": 1,
      "passengerName": "Nguyễn Văn A",
      "driverId": 2,
      "driverName": "Nguyễn Văn B",
      "departure": "Hà Nội",
      "destination": "Hải Phòng",
      "bookingTime": "2023-10-30T15:30:00",
      "startTime": "2023-11-01T08:00:00",
      "seats": 2,
      "price": 300000,
      "status": "PENDING"
    }
  ]
}
```

### Hủy đặt chỗ (hành khách)
```
PUT /api/passenger/cancel-bookings/{bookingId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Huỷ chuyến đi thành công",
  "data": {
    "id": 1,
    "rideId": 1,
    "passengerId": 1,
    "passengerName": "Nguyễn Văn A",
    "driverId": 2,
    "driverName": "Nguyễn Văn B",
    "departure": "Hà Nội",
    "destination": "Hải Phòng",
    "bookingTime": "2023-10-30T15:30:00",
    "startTime": "2023-11-01T08:00:00",
    "seats": 2,
    "price": 300000,
    "status": "CANCELLED"
  }
}
```

### Chấp nhận đặt chỗ (tài xế)
```
PUT /api/driver/accept/{bookingId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã chấp nhận hành khách",
  "data": null
}
```

### Từ chối đặt chỗ (tài xế)
```
PUT /api/driver/reject/{bookingId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã từ chối hành khách",
  "data": null
}
```

### Xác nhận hoàn thành chuyến đi (tài xế)
```
PUT /api/driver/complete/{rideId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Tài xế đã hoàn thành chuyến đi",
  "data": null
}
```

### Xác nhận hoàn thành chuyến đi (hành khách)
```
PUT /api/passenger/passenger-confirm/{rideId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Hành khách đã xác nhận hoàn thành",
  "data": null
}
```

## Thông tin người dùng

### Xem thông tin cá nhân (hành khách)
```
GET /api/passenger/profile
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Thông tin người dùng",
  "data": {
    "id": 1,
    "fullName": "Nguyễn Văn A",
    "email": "example@gmail.com",
    "phone": "0123456789",
    "avatarUrl": "http://example.com/images/avatar.jpg",
    "role": "PASSENGER"
  }
}
```

### Xem thông tin cá nhân (tài xế)
```
GET /api/driver/profile
Authorization: Bearer {token}
```

**Response:**
```json
{
  "id": 2,
  "status": "ACTIVE",
  "licenseImageUrl": "http://example.com/images/license.jpg",
  "vehicleImageUrl": "http://example.com/images/vehicle.jpg",
  "avatarUrl": "http://example.com/images/avatar.jpg",
  "fullName": "Nguyễn Văn B",
  "email": "driver@gmail.com",
  "phone": "0987654321",
  "role": "DRIVER"
}
```

## Quản lý tệp tin

### Tải lên hình ảnh
```
POST /api/files/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Parameters:**
- `file`: File cần tải lên

**Response:**
```json
{
  "success": true,
  "message": "Tải lên thành công",
  "data": {
    "url": "http://example.com/images/uploaded.jpg"
  }
}
```

## Quản trị viên

### Xem danh sách tài xế
```
GET /api/admin/drivers
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Danh sách tài xế",
  "data": [
    {
      "id": 2,
      "status": "PENDING",
      "licenseImageUrl": "http://example.com/images/license.jpg", 
      "vehicleImageUrl": "http://example.com/images/vehicle.jpg",
      "avatarUrl": "http://example.com/images/avatar.jpg",
      "fullName": "Nguyễn Văn B",
      "email": "driver@gmail.com",
      "phone": "0987654321",
      "role": "DRIVER"
    }
  ]
}
```

### Phê duyệt tài xế
```
PUT /api/admin/approve/{driverId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã phê duyệt tài xế",
  "data": {
    "id": 2,
    "status": "ACTIVE",
    "licenseImageUrl": "http://example.com/images/license.jpg",
    "vehicleImageUrl": "http://example.com/images/vehicle.jpg",
    "avatarUrl": "http://example.com/images/avatar.jpg",
    "fullName": "Nguyễn Văn B",
    "email": "driver@gmail.com",
    "phone": "0987654321",
    "role": "DRIVER"
  }
}
```

### Từ chối tài xế
```
PUT /api/admin/reject/{driverId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Đã từ chối tài xế",
  "data": {
    "id": 2,
    "status": "REJECTED",
    "licenseImageUrl": "http://example.com/images/license.jpg",
    "vehicleImageUrl": "http://example.com/images/vehicle.jpg",
    "avatarUrl": "http://example.com/images/avatar.jpg",
    "fullName": "Nguyễn Văn B",
    "email": "driver@gmail.com",
    "phone": "0987654321",
    "role": "DRIVER"
  }
}
```

## Tin nhắn và trò chuyện

### Gửi tin nhắn
```
POST /api/chat/send
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "receiverId": 2,
  "content": "Xin chào, tôi muốn đặt chuyến đi"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Gửi tin nhắn thành công",
  "data": {
    "id": 1,
    "senderId": 1,
    "receiverId": 2,
    "content": "Xin chào, tôi muốn đặt chuyến đi",
    "timestamp": "2023-10-30T16:45:30",
    "isRead": false
  }
}
```

### Lấy lịch sử trò chuyện
```
GET /api/chat/history/{userId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Lịch sử trò chuyện",
  "data": [
    {
      "id": 1, 
      "senderId": 1,
      "receiverId": 2,
      "content": "Xin chào, tôi muốn đặt chuyến đi",
      "timestamp": "2023-10-30T16:45:30",
      "isRead": true
    },
    {
      "id": 2,
      "senderId": 2, 
      "receiverId": 1,
      "content": "Xin chào, vâng ạ",
      "timestamp": "2023-10-30T16:47:30",
      "isRead": false
    }
  ]
}
```

## Mã trạng thái

### Trạng thái chuyến đi (RideStatus)
- `ACTIVE`: Chuyến đi đang hoạt động
- `COMPLETED`: Chuyến đi đã hoàn thành
- `CANCELLED`: Chuyến đi đã bị hủy
- `IN_PROGRESS`: Chuyến đi đang diễn ra

### Trạng thái đặt chỗ (BookingStatus)
- `PENDING`: Đang chờ xác nhận
- `ACCEPTED`: Đã được chấp nhận
- `REJECTED`: Đã bị từ chối
- `CANCELLED`: Đã bị hủy
- `COMPLETED`: Đã hoàn thành
- `DRIVER_CONFIRMED`: Tài xế đã xác nhận hoàn thành
- `PASSENGER_CONFIRMED`: Hành khách đã xác nhận hoàn thành

### Trạng thái tài xế (DriverStatus)
- `PENDING`: Đang chờ phê duyệt
- `ACTIVE`: Đã được phê duyệt
- `REJECTED`: Đã bị từ chối
- `SUSPENDED`: Đã bị tạm dừng

## Thông báo lỗi

Trong trường hợp lỗi, API sẽ trả về cấu trúc phản hồi như sau:

```json
{
  "success": false,
  "message": "Mô tả chi tiết về lỗi xảy ra",
  "data": null
}
```

Mã trạng thái HTTP:
- `200 OK`: Yêu cầu thành công
- `201 Created`: Tạo mới thành công
- `400 Bad Request`: Yêu cầu không hợp lệ
- `401 Unauthorized`: Chưa xác thực
- `403 Forbidden`: Không có quyền truy cập
- `404 Not Found`: Không tìm thấy tài nguyên
- `409 Conflict`: Xung đột (ví dụ: email đã tồn tại)
- `500 Internal Server Error`: Lỗi máy chủ

## Xác thực và bảo mật

Tất cả các API (ngoại trừ đăng ký và đăng nhập) đều yêu cầu xác thực bằng JWT token. Token phải được đặt trong header `Authorization` với tiền tố "Bearer":

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Token có thời hạn hữu dụng, sau khi hết hạn người dùng cần đăng nhập lại để lấy token mới. 

