# 🚗 Carpooling System

Hệ thống Carpooling (Đi chung xe) giúp kết nối tài xế và hành khách lại với nhau nhằm tối ưu chi phí, giảm ùn tắc giao thông và bảo vệ môi trường.

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
