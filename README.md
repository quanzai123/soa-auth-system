# 🛡️ Hệ Thống Xác Thực SOA (Google OAuth 2.0 & Stateless JWT)

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.1-brightgreen?style=for-the-badge&logo=springboot" alt="Spring Boot 4.1.1" />
  <img src="https://img.shields.io/badge/Spring_Security-6.0-green?style=for-the-badge&logo=springsecurity" alt="Spring Security 6" />
  <img src="https://img.shields.io/badge/OAuth_2.0-Google_SSO-4285F4?style=for-the-badge&logo=google" alt="Google OAuth" />
  <img src="https://img.shields.io/badge/JWT-RFC_7519-black?style=for-the-badge&logo=jsonwebtokens" alt="JWT" />
  <img src="https://img.shields.io/badge/Tests-100%25_Passed-success?style=for-the-badge&logo=postman" alt="Newman Passed" />
</p>

> **Đề tài Seminar:** Cổng Xác Thực Tập Trung Google OAuth 2.0 & Stateless JWT trong Kiến Trúc SOA  
> **Học phần:** Kiến Trúc Hướng Dịch Vụ (SOA - 504070) — Khoa Công Nghệ Thông Tin — Đại học Tôn Đức Thắng (TDTU)  
> **Nhóm sinh viên thực hiện:**  
> - **Nguyễn Anh Quân** — MSSV: `524H0122`  
> - **Nguyễn Hồng Quân** — MSSV: `524H0123`  
> **Giảng viên hướng dẫn:** Thầy Dương Hữu Phúc  

---

## ⚡ Hướng Dẫn Tải Về & Khởi Động Nhanh (Từ Số 0)

### 🪟 1. Dành Cho Windows (Sử dụng PowerShell)

Mở **PowerShell** (nhấn phím Windows, gõ `powershell`) và thực hiện theo 3 đoạn mã dưới đây:

#### 🔹 Đoạn 1: Bật TLS 1.2 và kiểm tra/cài đặt JDK 21
```powershell
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $ProgressPreference = 'SilentlyContinue'
if (!(Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "⚠️ Đang cài đặt Microsoft OpenJDK 21..." -ForegroundColor Cyan
    winget install Microsoft.OpenJDK.21 --silent --accept-package-agreements --accept-source-agreements
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}
```

#### 🔹 Đoạn 2: Tải mã nguồn sạch từ GitHub & giải nén (Không cần cài Git)
```powershell
if (Test-Path soa-auth-system-main) { Remove-Item -Recurse -Force soa-auth-system-main }
Invoke-WebRequest -Uri "https://github.com/quanzai123/soa-auth-system/archive/refs/heads/main.zip" -OutFile "soa.zip"
Expand-Archive -Path "soa.zip" -DestinationPath "." -Force
Set-Location "soa-auth-system-main"
```

#### 🔹 Đoạn 3: Khởi động Server
```powershell
.\run.bat
```

> 💡 **Mẹo gộp 1 dòng lệnh duy nhất trên Windows (Copy & Paste):**
> ```powershell
> [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $ProgressPreference = 'SilentlyContinue'; if (Test-Path soa-auth-system-main) { Remove-Item -Recurse -Force soa-auth-system-main }; Invoke-WebRequest -Uri "https://github.com/quanzai123/soa-auth-system/archive/refs/heads/main.zip" -OutFile "soa.zip"; Expand-Archive -Path "soa.zip" -DestinationPath "." -Force; Set-Location "soa-auth-system-main"; .\run.bat
> ```
> 💡 **Cách nhấp chuột không cần gõ lệnh (100% thành công):**  
> 1. Nếu máy chưa có Java 21: Tải bộ cài chính thức [Microsoft OpenJDK 21 (.msi)](https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.msi) và cài đặt trong 30 giây.  
> 2. Tải file ZIP mã nguồn: [soa-auth-system-main.zip](https://github.com/quanzai123/soa-auth-system/archive/refs/heads/main.zip)  
> 3. Chuột phải chọn **Extract All... (Giải nén)** → Mở thư mục và **nhấp đúp chuột vào file `run.bat`** là xong!

---

### 🍏 2. Dành Cho macOS / Linux (Sử dụng Terminal)

Mở **Terminal** và thực hiện theo 2 đoạn mã:

#### 🔹 Đoạn 1: Tải mã nguồn từ GitHub & di chuyển vào thư mục
```bash
rm -rf soa-auth-system && git clone https://github.com/quanzai123/soa-auth-system.git
cd soa-auth-system
```

#### 🔹 Đoạn 2: Cấp quyền thực thi & khởi động hệ thống
```bash
chmod +x mvnw run.sh start.command && ./run.sh
```

> 💡 **Mẹo gộp 1 dòng lệnh duy nhất trên Mac/Linux:**
> ```bash
> rm -rf soa-auth-system && git clone https://github.com/quanzai123/soa-auth-system.git && cd soa-auth-system && chmod +x mvnw run.sh start.command && ./run.sh
> ```
> 💡 **Cách nhấp chuột trên Mac:** Mở thư mục `soa-auth-system` và **nhấp đúp vào file `start.command`**!

---

## 🌐 Địa Chỉ Truy Cập Dịch Vụ

Sau khi khởi động, hệ thống tự động chạy và sẵn sàng truy cập tại:

| Dịch vụ | URL | Chức năng |
| :--- | :--- | :--- |
| **Cổng Xác Thực Web (SPA)** | [http://localhost:8080](http://localhost:8080) | Giao diện đăng nhập Google SSO, quản lý phiên và phân quyền |
| **Trình Xem Database Trực Tiếp** | [http://localhost:8080/database.html](http://localhost:8080/database.html) | Giám sát dữ liệu bảng `users` & `user_identities` theo thời gian thực |
| **Tài Liệu Kỹ Thuật Hệ Thống** | [http://localhost:8080/docs.html](http://localhost:8080/docs.html) | Đặc tả luồng OAuth 2.0 PKCE, Stateless JWT, HMAC-SHA384 |

> 📌 **Không cần cài đặt CSDL bên ngoài:** Ứng dụng mặc định kích hoạt CSDL nhúng **H2 In-Memory** chạy trực tiếp trong RAM, đã nạp sẵn tài khoản mẫu của nhóm sinh viên TDTU.  
> 🐳 **Nếu muốn chạy với Docker & SQL Server thật:** Gõ lệnh `./run.sh docker` (hoặc `.\run.bat docker`).

---

## 📮 Hướng Dẫn Tự Kiểm Thử Với Postman

File kịch bản kiểm thử nằm ngay tại thư mục gốc của repository: **`soa-auth-collection.json`**

1. Mở Postman → Chọn **Import** → Kéo thả file `soa-auth-collection.json` vào.
2. Quy trình kiểm thử 4 giai đoạn chuẩn SOA:
   * **Giai đoạn 1 (Public APIs):** Kiểm tra cấu hình Google SSO (`01`), lấy URL ủy quyền (`02`), chứng minh API Private bị chặn khi chưa có token (`03`).
   * **Giai đoạn 2 (Đồng bộ phiên):** Chạy `04. Đồng bộ phiên đăng nhập từ Web` → Tự động đồng bộ Access Token và Refresh Token vào Postman.
   * **Giai đoạn 3 (Private APIs):** Xem hồ sơ người dùng (`05`), xem danh sách liên kết Google (`06`), xem cơ sở dữ liệu (`07`), tự động gia hạn token (`08`).
   * **Giai đoạn 4 (Thu hồi quyền):** Đăng xuất đưa Token vào Blacklist (`09`), chứng minh dùng lại Token cũ bị chặn đứng `403 Forbidden` ngay lập tức (`10`).

---

## 🏛️ Điểm Sáng Kiến Trúc Của Đề Tài

1. **Stateless Authentication (Phi trạng thái):** Máy chủ không lưu trữ session trong bộ nhớ, toàn bộ danh tính người dùng được mã hóa vào Access Token có chữ ký số HMAC-SHA384.
2. **Loosely Coupled Identity (Ràng buộc lỏng):** Tách biệt thực thể người dùng nội bộ (`users`) và danh tính nhà cung cấp bên ngoài (`user_identities`), dễ dàng mở rộng sang Microsoft Office 365, Apple, VNeID.
3. **Automated Zero Trust Domain RBAC:** Tự động nhận diện và cấp quyền `STUDENT` cho email giáo dục (`*.edu.vn`, `*.edu`) và `PERSONAL` cho email cá nhân.
4. **Immediate Revocation via TTL Blacklist:** Giải quyết triệt để nhược điểm của JWT bằng bộ lọc chặn trước ngưỡng (Pre-filter Interception), thu hồi Token trong 0.1 mili-giây khi người dùng Logout.

---

## 📁 Cấu Trúc Mã Nguồn Tinh Gọn

```text
soa-auth-system/
├── pom.xml                               # Quản lý thư viện Maven & Metadata nhóm tác giả
├── mvnw / mvnw.cmd                       # Maven Wrapper (chạy không cần cài Maven)
├── run.sh / run.bat                      # Script 1-click khởi động cho macOS/Linux & Windows
├── start.command                         # Phím tắt nhấp đúp chuột trên macOS
├── docker-compose.yml                    # Cấu hình container Microsoft SQL Server 2022
├── soa-auth-collection.json              # Bộ kiểm thử tự động Postman (10 requests, 17 assertions)
├── HUONG_DAN_CAI_DAT_VA_CHAY.md          # Bản hướng dẫn tải & chạy chi tiết từ con số 0
└── src/
    ├── main/java/com/soa/auth/
    │   ├── SoaAuthSystemApplication.java # Điểm khởi động ứng dụng Spring Boot
    │   ├── config/                       # SecurityConfig, DataInitializer, ExceptionHandler
    │   ├── controller/                   # AuthController, UserController, DatabaseViewerController
    │   ├── dto/                          # AuthResponse, ApiResponse, GoogleTokenResponse
    │   ├── entity/                       # User, UserIdentity (Ràng buộc lỏng danh tính)
    │   ├── repository/                   # UserRepository, UserIdentityRepository
    │   ├── security/                     # JwtTokenProvider, JwtAuthenticationFilter, TokenBlacklist
    │   └── service/                      # AuthService, GoogleOAuthService, UserService
    └── main/resources/
        ├── application.yml               # Cấu hình Spring Boot & Database Profile
        ├── application-h2.yml            # Cấu hình H2 In-Memory siêu tốc
        └── static/                       # index.html, database.html, docs.html
```

---

## 📄 Tài Liệu Đính Kèm
* 📘 [Hướng dẫn cài đặt và khởi động chi tiết từ con số 0](HUONG_DAN_CAI_DAT_VA_CHAY.md)
* 📖 [Tài liệu đặc tả kiến trúc kỹ thuật trực tuyến](http://localhost:8080/docs.html)
* 🗄️ [Trang xem dữ liệu Database trực tiếp](http://localhost:8080/database.html)
