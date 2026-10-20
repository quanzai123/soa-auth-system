# 🛡️ Hệ Thống Xác Thực SOA (Google OAuth 2.0 & Stateless JWT)

> **Đề tài Seminar Kiến trúc Hướng dịch vụ (SOA - 504070)**  
> **Sinh viên:** Nguyễn Anh Quân — MSSV: `524H0122`  
> **Khoa:** Công Nghệ Thông Tin — Đại học Tôn Đức Thắng (TDTU)  
> **Giảng viên hướng dẫn:** Thầy Dương Hữu Phúc

---

## ⚡ Hướng Dẫn Khởi Động Nhanh Nhất (1 Dòng Lệnh Duy Nhất)

### 🪟 1. Dành Cho Windows (Không cần cài Git, tự cài Java nếu thiếu, chạy siêu tốc)
Mở **PowerShell** và dán đúng **1 dòng lệnh** sau rồi bấm **Enter**:

```powershell
if (!(Get-Command java -ErrorAction SilentlyContinue)) { Write-Host "⚠️ Đang cài đặt OpenJDK 21..." -ForegroundColor Cyan; winget install Microsoft.OpenJDK.21 --silent --accept-package-agreements --accept-source-agreements; $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User") }; if (Test-Path soa-auth-system-main) { Remove-Item -Recurse -Force soa-auth-system-main }; Invoke-WebRequest -Uri "https://github.com/quanzai123/soa-auth-system/archive/refs/heads/main.zip" -OutFile "soa.zip"; Expand-Archive -Path "soa.zip" -DestinationPath "." -Force; Set-Location "soa-auth-system-main"; .un.bat
```

> 💡 **Cách không cần gõ lệnh (Nhấp chuột):**  
> 1. Tải file ZIP: [Tải soa-auth-system-main.zip](https://github.com/quanzai123/soa-auth-system/archive/refs/heads/main.zip)  
> 2. Chuột phải vào file `.zip` $ightarrow$ Chọn **Extract All... (Giải nén tất cả)**  
> 3. Mở thư mục vừa giải nén $ightarrow$ **Nhấp đúp chuột vào file `run.bat`** là xong!

---

### 🍏 2. Dành Cho macOS / Linux (1 Dòng Lệnh Tự Động Từ Số 0)
Mở **Terminal** và dán đúng **1 dòng lệnh** sau rồi bấm **Enter**:

```bash
rm -rf soa-auth-system && git clone https://github.com/quanzai123/soa-auth-system.git && cd soa-auth-system && chmod +x mvnw run.sh start.command && ./run.sh
```

> 💡 **Cách nhấp đúp chuột trên Mac:** Mở thư mục dự án và **nhấp đúp vào file `start.command`**!

---

👉 **Sau khi khởi động, ứng dụng tự động chạy tại:** **[http://localhost:8080](http://localhost:8080)**  
*(Không cần cài SQL Server, không cần Docker. Hệ thống tự dùng CSDL nhúng H2 In-Memory chạy trực tiếp trong RAM)*.

---

### 🐳 3. Dành Cho Ai Muốn Chạy Với Docker & SQL Server 2022 Thật
Nếu máy đã cài Docker Desktop và muốn chạy với CSDL Microsoft SQL Server:
1. **Khởi động SQL Server qua Docker:** `docker compose up -d`
2. **Khởi động Backend:**  
   * macOS / Linux: `./run.sh docker` (hoặc `./mvnw spring-boot:run`)  
   * Windows: `.un.bat docker` (hoặc `mvnw.cmd spring-boot:run`)  
👉 **Trang quản lý Database trực tiếp:** [http://localhost:8080/database.html](http://localhost:8080/database.html)

---

## 📮 Hướng Dẫn Tự Test Với Postman (Đồng Bộ Trực Tiếp)

File kịch bản kiểm thử nằm ngay tại thư mục gốc: **`soa-auth-collection.json`**

### Quy trình tự test từng bước:
1. **Import vào Postman:**
   * Mở Postman $ightarrow$ Chọn **Import** $ightarrow$ Kéo thả file `soa-auth-collection.json` vào.
2. **Đăng nhập trên Web:**
   * Mở trình duyệt truy cập [http://localhost:8080](http://localhost:8080).
   * Bấm nút **"Đăng nhập an toàn với Google"** và đăng nhập bằng tài khoản Google của bạn.
   * Hệ thống sẽ tự động phân loại quyền hạn:
     - Email trường học (`.edu.vn`, `.edu`, `@student.tdtu.edu.vn`) $ightarrow$ Cấp quyền `STUDENT`.
     - Email cá nhân thông thường (`@gmail.com`) $ightarrow$ Cấp quyền `PERSONAL`.
3. **Đồng bộ Token sang Postman:**
   * Chạy Request `04. Đồng bộ phiên đăng nhập từ Web` $ightarrow$ Postman tự động cập nhật Token mới nhất từ trình duyệt vào cả Collection Variables và Environment!
4. **Kiểm thử các API nghiệp vụ:**
   * Chạy request `05. Private - Xem Hồ Sơ Người Dùng (/api/users/me)`: Trả về **200 OK**.
   * Chạy request `07. Private - Xem Cơ Sở Dữ Liệu SQL Server (/api/users/database-viewer)`: Trả về **200 OK**.
   * Chạy request `08. Private - Tự Động Gia Hạn Bằng Refresh Token`: Đổi token mới và reset bộ đếm 15 phút.
   * Chạy request `09. Đăng Xuất Tài Khoản & Thu Hồi Token Vào Blacklist`: Trả về **200 OK**.
   * Chạy request `10. [Kiểm Chứng] Dùng Lại Token Cũ Sau Đăng Xuất`: Bị chặn **403 Forbidden** ngay lập tức (Chứng minh thu hồi thành công).

---

## 🏛️ Điểm Sáng Kiến Trúc SOA Của Đồ Án
1. **Stateless Authentication (Phi trạng thái):** Máy chủ không lưu session trong RAM, toàn bộ danh tính được nén vào Access Token có chữ ký HMAC-SHA256.
2. **Loosely Coupled Identity (Ràng buộc lỏng):** Tách biệt thực thể người dùng nội bộ (`users`) và danh tính bên ngoài (`user_identities`), dễ dàng mở rộng sang Microsoft, Apple, VNeID.
3. **Automated Zero Trust Domain RBAC:** Tự động nhận diện và phân quyền theo tên miền email chuẩn tổ chức giáo dục.
4. **Bounded TTL Blacklist:** Giải quyết triệt để bài toán thu hồi Token tức thời khi Logout trong kiến trúc Stateless mà không lo phình bộ nhớ RAM.
