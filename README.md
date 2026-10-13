# 🛡️ Hệ Thống Xác Thực SOA (Google OAuth 2.0 & Stateless JWT)

> **Đề tài Seminar Kiến trúc Hướng dịch vụ (SOA)**  
> **Sinh viên:** Nguyễn Anh Quân — MSSV: `524H0122`  
> **Khoa:** Công Nghệ Thông Tin — Đại học Tôn Đức Thắng (TDTU)

---

## 📂 Thành Phần Trong Dự Án
1. **Mã nguồn Spring Boot Backend & Giao diện Web** (`src/`, `pom.xml`, `mvnw`)
2. **Kịch bản kiểm thử Postman hoàn chỉnh** (`soa-auth-collection.json`)

---

## ⚡ Hướng Dẫn Cài Đặt & Chạy Ứng Dụng (3 Bước)

### Bước 1: Khởi động SQL Server 2022 (Docker)
Chạy lệnh sau trong Terminal / Command Prompt:
```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Spring@123456" \
   -p 1433:1433 --name sqlserver --hostname sqlserver \
   -d mcr.microsoft.com/mssql/server:2022-latest
```

### Bước 2: Chạy ứng dụng Backend
Dự án đã tích hợp sẵn Maven Wrapper, không cần cài đặt Maven trước.

* **Trên macOS / Linux:**
  ```bash
  ./mvnw spring-boot:run
  ```
* **Trên Windows (cmd / PowerShell):**
  ```bash
  mvnw.cmd spring-boot:run
  ```

### Bước 3: Mở Giao diện Web
Khi ứng dụng khởi động xong, truy cập:
* **Cổng Xác Thực Web:** [http://localhost:8080](http://localhost:8080)
* **Trình xem Database trực tiếp:** [http://localhost:8080/database.html](http://localhost:8080/database.html)

---

## 📮 Hướng Dẫn Tự Test Với Postman (Đồng Bộ Trực Tiếp)

File kịch bản kiểm thử nằm ngay tại thư mục gốc: **`soa-auth-collection.json`**

### Quy trình tự test từng bước:
1. **Import vào Postman:**
   * Mở Postman $\rightarrow$ Chọn **Import** $\rightarrow$ Chọn file `soa-auth-collection.json`.
2. **Lấy Token từ Web:**
   * Truy cập [http://localhost:8080](http://localhost:8080).
   * Bạn có thể bấm **Đăng nhập Google** hoặc bấm nút **"Đăng nhập nhanh Sinh viên TDTU"** (Hệ thống có sẵn dữ liệu mẫu, không cần cấu hình Google Cloud vẫn test được ngay).
3. **Chạy các API trong Postman:**
   * Chạy lần lượt các Request từ **01 đến 10** trong Collection:
     - **01 - 02:** Kiểm tra API Public (Cấu hình Google OAuth, Lấy URL SSO).
     - **03:** Thử gọi API Private khi chưa đăng nhập (Bị chặn 403 Forbidden).
     - **04:** Tự động đồng bộ phiên vừa đăng nhập trên Web vào Postman (Tự lưu Token vào biến môi trường).
     - **05 - 07:** Gọi các API Private (Xem thông tin cá nhân, Danh sách liên kết, Dữ liệu SQL Server).
     - **08:** Thử tính năng Refresh Token $\rightarrow$ Cấp token mới và reset đồng hồ về đủ 15 phút.
     - **09 - 10:** Đăng xuất tài khoản (Đưa token vào Blacklist) và kiểm chứng token cũ bị từ chối truy cập.

---

## 👥 Tài Khoản Dùng Thử Có Sẵn (Zero-Config)
Hệ thống đã nạp sẵn 2 tài khoản mẫu trong Database để bạn test ngay lập tức:

| Tài khoản | Email | Phân quyền (RBAC) | Loại tài khoản |
| :--- | :--- | :--- | :--- |
| **Sinh viên TDTU** | `524h0122@student.tdtu.edu.vn` | `ROLE_STUDENT` | `STUDENT` |
| **Tài khoản cá nhân** | `quan03313@gmail.com` | `ROLE_PERSONAL` | `PERSONAL` |

---

## 🛠️ Danh Sách API (RESTful Endpoints)

| Phương thức | Endpoint | Quyền truy cập | Mô tả chức năng |
| :---: | :--- | :---: | :--- |
| `GET` | `/api/auth/google/url` | Public | Lấy URL đăng nhập Google OAuth 2.0 |
| `GET` | `/api/auth/google/config` | Public | Lấy cấu hình Client ID & Redirect URI |
| `POST` | `/api/auth/google/callback` | Public | Trao đổi Authorization Code lấy Token JWT |
| `POST` | `/api/auth/quick-login` | Public | Đăng nhập nhanh tài khoản thử nghiệm |
| `GET` | `/api/auth/latest-token` | Public | Tự động đồng bộ phiên từ Web sang Postman |
| `POST` | `/api/auth/refresh-token` | Public | Cấp lại Access Token mới (15 phút) bằng Refresh Token |
| `POST` | `/api/auth/logout` | Bearer Token | Đăng xuất & đưa token vào Blacklist |
| `GET` | `/api/users/me` | Bearer Token | Xem thông tin hồ sơ người dùng |
| `GET` | `/api/users/me/linked-accounts` | Bearer Token | Xem danh sách tài khoản liên kết Google |
| `GET` | `/api/users/database-viewer` | Bearer Token | Xem các bản ghi trong SQL Server 2022 |

---

## 🧪 Chạy Test Tự Động Bằng Dòng Lệnh (Tùy chọn)
Nếu muốn chạy kiểm thử tự động toàn bộ 10 API bằng dòng lệnh (Newman):
```bash
npx --yes newman run soa-auth-collection.json
```
