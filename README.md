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

---

### Authentication API

#### 1. Đăng ký tài khoản hành khách

`POST /api/auth/passenger-register`

Đăng ký một người dùng mới với vai trò hành khách.

**Parameters:**

*   `email` (query, required): Email đăng nhập của người dùng.
*   `password` (query, required): Mật khẩu của người dùng.
*   `fullName` (query, required): Họ và tên của người dùng.
*   `phone` (query, required): Số điện thoại của người dùng.

**Request Body (multipart/form-data):**

*   `avatarImage` (tùy chọn, binary): Ảnh đại diện của người dùng.

**Responses:**

*   `201 Created`: Đăng ký thành công. (`ApiResponseUserDTO`)
*   `400 Bad Request`: Dữ liệu không hợp lệ. (`ApiResponseUserDTO`)
*   `409 Conflict`: Email đã tồn tại. (`ApiResponseUserDTO`)

#### 2. Đăng ký tài khoản tài xế

`POST /api/auth/driver-register`

Đăng ký một người dùng mới với vai trò tài xế, bao gồm thông tin xe.

**Parameters:**

*   `email` (query, required): Email đăng nhập của tài xế.
*   `password` (query, required): Mật khẩu của tài xế.
*   `fullName` (query, required): Họ và tên của tài xế.
*   `phone` (query, required): Số điện thoại của tài xế.
*   `licensePlate` (query, required): Biển số xe.
*   `brand` (query, required): Hãng xe.
*   `model` (query, required): Mẫu xe.
*   `color` (query, required): Màu xe.
*   `numberOfSeats` (query, required): Số ghế.

**Request Body (multipart/form-data):**

*   `avatarImage` (tùy chọn, binary): Ảnh đại diện của tài xế.
*   `licenseImage` (tùy chọn, binary): Ảnh giấy phép lái xe.
*   `vehicleImage` (tùy chọn, binary): Ảnh xe.

**Responses:**

*   `201 Created`: Đăng ký thành công. (`ApiResponseDriverDTO`)
*   `400 Bad Request`: Dữ liệu không hợp lệ. (`ApiResponseDriverDTO`)
*   `409 Conflict`: Email đã tồn tại. (`ApiResponseDriverDTO`)

#### 3. Đăng nhập người dùng

`POST /api/auth/login`

Xác thực người dùng và trả về token JWT.

**Request Body (application/json):**

*   `email` (string): Email người dùng.
*   `password` (string): Mật khẩu người dùng.

**Responses:**

*   `200 OK`: Đăng nhập thành công. (`ApiResponseObject`)
*   `401 Unauthorized`: Thông tin đăng nhập không hợp lệ. (`ApiResponseObject`)

---

### User API

#### 1. Cập nhật thông tin cá nhân

`PUT /api/user/update-profile`

Thay đổi thông tin cá nhân của người dùng đã xác thực.

**Request Body (application/json):**

`UserUpdateRequestDTO`

**Responses:**

*   `200 OK`: Thay đổi thông tin thành công. (`ApiResponseObject`)
*   `400 Bad Request`: Thay đổi thông tin thất bại (dữ liệu không hợp lệ). (`ApiResponseObject`)

#### 2. Thay đổi mật khẩu

`PUT /api/user/change-pass`

Nhập mật khẩu cũ để thay đổi mật khẩu.

**Request Body (application/json):**

`ChangePassDTO`

**Responses:**

*   `200 OK`: Thay đổi mật khẩu thành công. (`ApiResponseObject`)
*   `400 Bad Request`: Thay đổi mật khẩu thất bại. (`ApiResponseObject`)

---

### Passenger API

#### 1. Lấy thông tin cá nhân hành khách

`GET /api/passenger/profile`

Trả về thông tin chi tiết của hành khách đã đăng nhập.

**Responses:**

*   `200 OK`: Lấy thông tin thành công. (`ApiResponseUserDTO`)
*   `404 Not Found`: Không tìm thấy người dùng. (`ApiResponseUserDTO`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseUserDTO`)

#### 2. Đặt chỗ cho chuyến đi

`POST /api/passenger/booking/{rideId}`

Hành khách đặt chỗ cho một chuyến đi cụ thể.

**Parameters:**

*   `rideId` (path, required): ID của chuyến đi cần đặt chỗ.
*   `seats` (query, required): Số ghế cần đặt.

**Responses:**

*   `200 OK`: Đặt chỗ thành công. (`ApiResponseBookingDTO`)
*   `400 Bad Request`: Đặt chỗ thất bại (ví dụ: không đủ ghế, chuyến đi không hợp lệ). (`ApiResponseBookingDTO`)

#### 3. Hủy đặt chỗ

`PUT /api/passenger/cancel-bookings/{rideId}`

Hành khách hủy đặt chỗ cho một chuyến đi.

**Parameters:**

*   `rideId` (path, required): ID của chuyến đi cần hủy đặt chỗ.

**Responses:**

*   `200 OK`: Hủy đặt chỗ thành công. (`ApiResponseBookingDTO`)
*   `400 Bad Request`: Hủy đặt chỗ thất bại. (`ApiResponseBookingDTO`)

#### 4. Xác nhận hoàn thành chuyến đi (Hành khách)

`PUT /api/passenger/passenger-confirm/{rideId}`

Hành khách xác nhận đã hoàn thành chuyến đi.

**Parameters:**

*   `rideId` (path, required): ID của chuyến đi cần xác nhận.

**Responses:**

*   `200 OK`: Xác nhận thành công. (`ApiResponseBookingDTO`)
*   `400 Bad Request`: Xác nhận thất bại. (`ApiResponseBookingDTO`)

#### 5. Lấy danh sách đặt chỗ của hành khách

`GET /api/passenger/bookings`

Trả về danh sách các đặt chỗ của hành khách đã đăng nhập.

**Responses:**

*   `200 OK`: Lấy danh sách thành công. (`ApiResponseListBookingDTO`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseListBookingDTO`)

#### 6. Lấy chi tiết đặt chỗ

`GET /api/passenger/booking/{bookingId}`

Trả về thông tin chi tiết của một đặt chỗ cụ thể.

**Parameters:**

*   `bookingId` (path, required): ID của booking cần xem chi tiết.

**Responses:**

*   `200 OK`: Lấy thông tin thành công. (`ApiResponseBookingDTO`)
*   `403 Forbidden`: Không có quyền truy cập. (`ApiResponseBookingDTO`)
*   `404 Not Found`: Không tìm thấy booking. (`ApiResponseBookingDTO`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseBookingDTO`)

---

### Driver API

#### 1. Lấy thông tin cá nhân tài xế

`GET /api/driver/profile`

Trả về thông tin chi tiết của tài xế đã đăng nhập.

**Responses:**

*   `200 OK`: Lấy thông tin thành công. (`ApiResponseObject`)
*   `401 Unauthorized`: Không có quyền truy cập. (`ApiResponseObject`)
*   `404 Not Found`: Không tìm thấy người dùng. (`ApiResponseObject`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseObject`)

#### 2. Chấp nhận đặt chỗ

`PUT /api/driver/accept/{bookingId}`

Tài xế chấp nhận yêu cầu đặt chỗ của hành khách.

**Parameters:**

*   `bookingId` (path, required): ID của booking cần chấp nhận.

**Responses:**

*   `200 OK`: Chấp nhận thành công. (`ApiResponseString`)
*   `404 Not Found`: Không tìm thấy booking. (`ApiResponseString`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseString`)

#### 3. Từ chối đặt chỗ

`PUT /api/driver/reject/{bookingId}`

Tài xế từ chối yêu cầu đặt chỗ của hành khách.

**Parameters:**

*   `bookingId` (path, required): ID của booking cần từ chối.

**Responses:**

*   `200 OK`: Từ chối thành công. (`ApiResponseString`)
*   `404 Not Found`: Không tìm thấy booking. (`ApiResponseString`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseString`)

#### 4. Xác nhận hoàn thành chuyến đi (Tài xế)

`PUT /api/driver/complete/{rideId}`

Tài xế xác nhận đã hoàn thành chuyến đi.

**Parameters:**

*   `rideId` (path, required): ID của chuyến đi cần xác nhận hoàn thành.

**Responses:**

*   `200 OK`: Xác nhận thành công. (`ApiResponseString`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseString`)

#### 5. Lấy danh sách chuyến đi của tài xế

`GET /api/driver/my-rides`

Trả về danh sách các chuyến đi do tài xế đã đăng nhập tạo ra.

**Responses:**

*   `200 OK`: Lấy danh sách thành công. (`ApiResponseListRideRequestDTO`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseListRideRequestDTO`)

#### 6. Lấy danh sách đặt chỗ của tài xế

`GET /api/driver/bookings`

Trả về danh sách các đặt chỗ cho các chuyến đi của tài xế đã đăng nhập.

**Responses:**

*   `200 OK`: Lấy danh sách thành công. (`ApiResponseListBookingDTO`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseListBookingDTO`)

---

### Rides API

#### 1. Tạo chuyến đi

`POST /api/ride`

Tài xế tạo một chuyến đi mới với thông tin điểm đi, điểm đến, thời gian và số ghế.

**Request Body (application/json):**

`RideRequestDTO`

**Responses:**

*   `201 Created`: Tạo chuyến đi thành công. (`ApiResponseObject`)
*   `400 Bad Request`: Yêu cầu không hợp lệ. (`ApiResponseObject`)
*   `500 Internal Server Error`: Lỗi hệ thống khi tạo chuyến đi. (`ApiResponseObject`)

#### 2. Cập nhật chuyến đi

`PUT /api/ride/update/{id}`

Tài xế cập nhật thông tin chuyến đi đã tạo.

**Parameters:**

*   `id` (path, required): ID của chuyến đi cần cập nhật.

**Request Body (application/json):**

`RideRequestDTO`

**Responses:**

*   `200 OK`: Cập nhật chuyến đi thành công. (`ApiResponseObject`)
*   `400 Bad Request`: Yêu cầu không hợp lệ. (`ApiResponseObject`)
*   `500 Internal Server Error`: Lỗi hệ thống khi cập nhật chuyến đi. (`ApiResponseObject`)

#### 3. Hủy chuyến đi

`PUT /api/ride/cancel/{id}`

Tài xế có thể hủy một chuyến đi theo ID.

**Parameters:**

*   `id` (path, required): ID của chuyến đi cần hủy.

**Responses:**

*   `200 OK`: Hủy bỏ chuyến đi thành công. (`ApiResponseObject`)
*   `400 Bad Request`: Yêu cầu không hợp lệ. (`ApiResponseObject`)
*   `500 Internal Server Error`: Lỗi hệ thống khi hủy chuyến đi. (`ApiResponseObject`)

#### 4. Xem chi tiết chuyến đi

`GET /api/ride/{id}`

Xem thông tin chi tiết của chuyến đi theo ID.

**Parameters:**

*   `id` (path, required): ID của chuyến đi.

**Responses:**

*   `200 OK`: Lấy thông tin chuyến đi thành công. (`ApiResponseObject`)
*   `404 Not Found`: Chuyến đi không tồn tại. (`ApiResponseObject`)
*   `500 Internal Server Error`: Lỗi khi lấy thông tin chuyến đi. (`ApiResponseObject`)

#### 5. Tìm kiếm chuyến đi

`GET /api/ride/search`

Hành khách có thể tìm kiếm chuyến đi theo điểm đi, điểm đến, ngày khởi hành và số ghế trống.

**Parameters:**

*   `departure` (query, optional): Điểm đi.
*   `destination` (query, optional): Điểm đến.
*   `startTime` (query, optional, date): Ngày khởi hành.
*   `seats` (query, optional): Số ghế trống.

**Responses:**

*   `200 OK`: Tìm chuyến đi thành công. (`ApiResponseListRideRequestDTO`)
*   `500 Internal Server Error`: Lỗi khi tìm chuyến đi. (`ApiResponseListRideRequestDTO`)

#### 6. Danh sách chuyến đi đang hoạt động

`GET /api/ride/available`

Lấy tất cả chuyến đi còn hiệu lực (chưa hủy, chưa hoàn thành).

**Responses:**

*   `200 OK`: Danh sách chuyến đi đang hoạt động. (`ApiResponseListRideRequestDTO`)
*   `500 Internal Server Error`: Lỗi khi lấy danh sách chuyến đi. (`ApiResponseListRideRequestDTO`)

#### 7. Lấy tất cả chuyến đi

`GET /api/ride/all-rides`

Trả về danh sách toàn bộ chuyến đi trong hệ thống (dành cho quản trị hoặc tài xế).

**Responses:**

*   `200 OK`: Tất cả chuyến đi. (`ApiResponseObject`)
*   `500 Internal Server Error`: Lỗi khi lấy tất cả chuyến đi. (`ApiResponseObject`)

---

### Chat API

#### 1. Lấy danh sách phòng chat

`GET /api/chat/rooms`

Trả về danh sách các phòng chat của người dùng hiện tại.

**Responses:**

*   `200 OK`: Lấy danh sách thành công. (`ApiResponseListMapStringObject`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseListMapStringObject`)

#### 2. Lấy ID phòng chat

`GET /api/chat/room/{otherUserEmail}`

Tạo hoặc lấy ID phòng chat giữa người dùng hiện tại và một người dùng khác.

**Parameters:**

*   `otherUserEmail` (path, required): Email của người dùng khác.

**Responses:**

*   `200 OK`: Lấy ID phòng chat thành công. (`ApiResponseString`)
*   `404 Not Found`: Không tìm thấy người dùng. (`ApiResponseString`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseString`)

#### 3. Lấy tin nhắn của phòng chat

`GET /api/chat/{roomId}`

Trả về danh sách tin nhắn của một phòng chat cụ thể.

**Parameters:**

*   `roomId` (path, required): ID của phòng chat.

**Responses:**

*   `200 OK`: Lấy tin nhắn thành công. (`ApiResponseListChatMessageDTO`)
*   `403 Forbidden`: Không có quyền truy cập phòng chat. (`ApiResponseListChatMessageDTO`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseListChatMessageDTO`)

#### 4. Đánh dấu tin nhắn đã đọc

`PUT /api/chat/{roomId}/mark-read`

Đánh dấu tất cả tin nhắn trong phòng chat là đã đọc.

**Parameters:**

*   `roomId` (path, required): ID của phòng chat.

**Responses:**

*   `200 OK`: Đánh dấu thành công. (`ApiResponseVoid`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseVoid`)

#### 5. Gửi tin nhắn qua HTTP (chủ yếu dùng để test)

`POST /api/chat/test/{roomId}`

Gửi tin nhắn qua HTTP thay vì WebSocket.

**Parameters:**

*   `roomId` (path, required): ID của phòng chat.

**Request Body (application/json):**

`ChatMessageDTO`

**Responses:**

*   `200 OK`: Gửi tin nhắn thành công. (`ApiResponseChatMessageDTO`)
*   `401 Unauthorized`: Không tìm thấy người dùng. (`ApiResponseChatMessageDTO`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseChatMessageDTO`)

---

### Notifications API

#### 1. Lấy danh sách thông báo

`GET /api/notifications`

Trả về danh sách thông báo của người dùng đã đăng nhập.

**Responses:**

*   `200 OK`: Lấy danh sách thành công. (`ApiResponseListNotification`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseListNotification`)

#### 2. Đánh dấu thông báo đã đọc

`PUT /api/notifications/{id}/read`

Đánh dấu một thông báo cụ thể là đã đọc.

**Parameters:**

*   `id` (path, required): ID của thông báo cần đánh dấu.

**Responses:**

*   `200 OK`: Đánh dấu thành công. (`ApiResponseVoid`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseVoid`)

#### 3. Đánh dấu tất cả thông báo đã đọc

`PUT /api/notifications/read-all`

Đánh dấu tất cả thông báo của người dùng là đã đọc.

**Responses:**

*   `200 OK`: Đánh dấu thành công. (`ApiResponseVoid`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseVoid`)

#### 4. Đếm số thông báo chưa đọc

`GET /api/notifications/unread-count`

Trả về số lượng thông báo chưa đọc của người dùng.

**Responses:**

*   `200 OK`: Đếm thành công. (`ApiResponseLong`)
*   `500 Internal Server Error`: Lỗi máy chủ. (`ApiResponseLong`)

---

### Tracking API

#### 1. Test gửi vị trí tài xế

`POST /api/tracking/test/{rideId}`

Dùng để Swagger test, phát hành vị trí qua Redis.

**Parameters:**

*   `rideId` (path, required): ID của chuyến đi.
*   `Authorization` (header, required): JWT token.

**Request Body (application/json):**

`TrackingPayloadDTO`

**Responses:**

*   `200 OK`: OK. (`ApiResponseTrackingPayloadDTO`)

---

### Admin API

#### 1. Lấy thông tin chi tiết của người dùng

`GET /api/admin/user/{id}`

Trả về thông tin chi tiết của người dùng theo ID.

**Parameters:**

*   `id` (path, required): ID của người dùng cần xem thông tin.

**Responses:**

*   `200 OK`: Lấy thông tin người dùng thành công. (`ApiResponseDriverDTO`)
*   `400 Bad Request`: Không thể tìm thấy người dùng. (`ApiResponseDriverDTO`)

#### 2. Lấy danh sách người dùng theo vai trò

`GET /api/admin/user/role`

Trả về danh sách người dùng theo vai trò được chỉ định. Nếu không có vai trò nào được chỉ định, trả về tất cả người dùng.

**Parameters:**

*   `role` (query, optional): Vai trò của người dùng (ADMIN, DRIVER, PASSENGER).

**Responses:**

*   `200 OK`: Lấy danh sách người dùng thành công. (`ApiResponseListObject`)
*   `500 Internal Server Error`: Lỗi máy chủ khi lấy danh sách người dùng. (`ApiResponseListObject`)

#### 3. Chấp nhận đăng ký tài xế

`POST /api/admin/user/approved/{id}`

Chấp nhận đăng ký tài xế và gửi thông báo cho người dùng.

**Parameters:**

*   `id` (path, required): ID của người dùng (tài xế) cần chấp nhận.

**Responses:**

*   `200 OK`: Chấp nhận tài xế thành công. (`ApiResponseObject`)
*   `400 Bad Request`: Chấp nhận tài xế thất bại. (`ApiResponseObject`)

#### 4. Từ chối đăng ký tài xế

`POST /api/admin/user/reject/{id}`

Từ chối đăng ký tài xế và gửi thông báo cho người dùng với lý do từ chối.

**Parameters:**

*   `id` (path, required): ID của người dùng (tài xế) cần từ chối.
*   `rejectionReason` (query, required): Lý do từ chối đăng ký tài xế.

**Responses:**

*   `200 OK`: Từ chối tài xế thành công. (`ApiResponseBoolean`)
*   `400 Bad Request`: Từ chối tài xế thất bại. (`ApiResponseBoolean`)

#### 5. Xóa người dùng

`DELETE /api/admin/user/delete/{id}`

Xóa người dùng khỏi hệ thống theo ID và gửi thông báo cho người dùng đó.

**Parameters:**

*   `id` (path, required): ID của người dùng cần xóa.

**Responses:**

*   `200 OK`: Xóa người dùng thành công. (`ApiResponseObject`)
*   `404 Not Found`: Không tìm thấy người dùng. (`ApiResponseObject`)
*   `500 Internal Server Error`: Lỗi máy chủ khi xóa người dùng. (`ApiResponseObject`)

---

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

