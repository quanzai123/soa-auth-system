# 🛡️ Hệ Thống Xác Thực SOA (Google OAuth 2.0 & Stateless JWT)

> **Đề tài Seminar Kiến trúc Hướng dịch vụ (SOA - 504070)**  
> **Sinh viên:** Nguyễn Anh Quân — MSSV: `524H0122`  
> **Khoa:** Công Nghệ Thông Tin — Đại học Tôn Đức Thắng (TDTU)  
> **Giảng viên hướng dẫn:** Thầy Dương Hữu Phúc

---

## ⚡ Hướng Dẫn Tải & Chạy Nhanh Nhất (Quick Start)

### 🚀 Cách 1: 1 CÂU LỆNH DUY NHẤT (Chạy Siêu Tốc - Không cần cài Database)
Không cần cài đặt SQL Server hay Docker, dự án tự động sử dụng CSDL nhúng H2 In-Memory:

* **Tải mã nguồn về:**
  ```bash
  git clone https://github.com/quanzai123/soa-auth-system.git
  cd soa-auth-system
  ```

* **Chạy ứng dụng:**
  * **Trên macOS / Linux:**
    ```bash
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
    ```
    *(hoặc gõ script tự động: `./run.sh`)*

  * **Trên Windows (cmd / PowerShell):**
    ```cmd
    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
    ```
    *(hoặc chỉ cần **nhấp đúp chuột** vào file: `run.bat`)*

👉 **Mở trình duyệt truy cập ngay:** [http://localhost:8080](http://localhost:8080)

---

### 🐳 Cách 2: 2 CÂU LỆNH VỚI DOCKER & SQL SERVER 2022 THẬT
Nếu máy của bạn đã cài Docker và muốn chạy chuẩn với CSDL Microsoft SQL Server 2022:

1. **Khởi động SQL Server 2022:**
   ```bash
   docker compose up -d
   ```
2. **Khởi động Spring Boot:**
   * **macOS / Linux:** `./mvnw spring-boot:run`
   * **Windows:** `mvnw.cmd spring-boot:run`

👉 **Mở trình duyệt truy cập:** [http://localhost:8080](http://localhost:8080)  
👉 **Trang quản lý Database trực tiếp:** [http://localhost:8080/database.html](http://localhost:8080/database.html)

---

## 📮 Hướng Dẫn Tự Test Với Postman (Đồng Bộ Trực Tiếp)

File kịch bản kiểm thử nằm ngay tại thư mục gốc: **`soa-auth-collection.json`**

### Quy trình tự test từng bước:
1. **Import vào Postman:**
   * Mở Postman $\rightarrow$ Chọn **Import** $\rightarrow$ Kéo thả file `soa-auth-collection.json` vào.
2. **Đăng nhập trên Web:**
   * Mở trình duyệt truy cập [http://localhost:8080](http://localhost:8080).
   * Bấm nút **"Đăng nhập an toàn với Google"** và đăng nhập bằng tài khoản Google của bạn.
   * Hệ thống sẽ tự động phân loại quyền hạn:
     - Email trường học (`.edu.vn`, `.edu`, `@student.tdtu.edu.vn`) $\rightarrow$ Cấp quyền `STUDENT`.
     - Email cá nhân thông thường (`@gmail.com`) $\rightarrow$ Cấp quyền `PERSONAL`.
3. **Đồng bộ Token sang Postman:**
   * **Cách A (Tự động):** Trong Postman, chạy Request `00. Đồng bộ phiên đăng nhập từ Web` $\rightarrow$ Postman tự động bắt Token mới nhất từ trình duyệt!
   * **Cách B (File môi trường):** Trên giao diện Web, bấm nút **"Xuất môi trường Postman"** $\rightarrow$ Import file tải về vào Postman.
4. **Kiểm thử các API nghiệp vụ:**
   * Chạy request `04. Lấy tài nguyên Sinh viên (Yêu cầu STUDENT)`:
     - Nếu đăng nhập bằng email TDTU: Trả về **200 OK** kèm tài nguyên sinh viên.
     - Nếu đăng nhập bằng Gmail cá nhân: Trả về **403 Forbidden** (Chứng minh tính năng phân quyền Domain RBAC thành công 100%).
   * Chạy request `07. Thu hồi phiên / Đăng xuất (Immediate Revocation)`:
     - Token lập tức bị đưa vào Blacklist trong RAM.
     - Thử gọi lại API sẽ bị chặn ngay lập tức.

---

## 🏛️ Điểm Sáng Kiến Trúc SOA Của Đồ Án
1. **Stateless Authentication (Phi trạng thái):** Máy chủ không lưu session trong RAM, toàn bộ danh tính được nén vào Access Token có chữ ký HMAC-SHA256.
2. **Loosely Coupled Identity (Ràng buộc lỏng):** Tách biệt thực thể người dùng nội bộ (`users`) và danh tính bên ngoài (`user_identities`), dễ dàng mở rộng sang Microsoft, Apple, VNeID.
3. **Automated Zero Trust Domain RBAC:** Tự động nhận diện và phân quyền theo tên miền email chuẩn tổ chức giáo dục.
4. **Bounded TTL Blacklist:** Giải quyết triệt để bài toán thu hồi Token tức thời khi Logout trong kiến trúc Stateless mà không lo phình bộ nhớ.
