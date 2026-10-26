# ⚡ HƯỚNG DẪN TẢI VỀ & CHẠY DỰ ÁN TỪ CON SỐ 0
> **Đề tài Seminar:** Cổng Xác Thực Tập Trung Google OAuth 2.0 & Stateless JWT trong Kiến Trúc SOA  
> **Học phần:** Kiến Trúc Hướng Dịch Vụ (SOA - 504070) — Đại học Tôn Đức Thắng (TDTU)  
> **Nhóm sinh viên thực hiện:**  
> - Nguyễn Anh Quân — MSSV: `524H0122`  
> - Nguyễn Hồng Quân — MSSV: `524H0123`  
> **Giảng viên hướng dẫn:** Thầy Dương Hữu Phúc

---

Tài liệu này hướng dẫn chi tiết cách tải về và khởi động dự án chỉ bằng **2 - 3 đoạn mã** trên Terminal (macOS/Linux) hoặc PowerShell (Windows), không cần cài đặt phức tạp, hệ thống tự động thiết lập từ con số 0.

---

## 🪟 PHẦN 1: DÀNH CHO MÁY WINDOWS (Sử dụng PowerShell)

Nhấn phím **Windows**, gõ `PowerShell` rồi mở ứng dụng. Bạn có thể chọn thực hiện theo **Cách 1** (từng bước rõ ràng) hoặc **Cách 2** (1 dòng lệnh duy nhất).

### Cách 1: Chạy theo 3 đoạn mã tuần tự (Khuyên dùng)

#### 🔹 Đoạn 1: Tự động kiểm tra & cài đặt Microsoft OpenJDK 21 (nếu máy chưa có)
Copy đoạn mã sau dán vào PowerShell và nhấn **Enter**:
```powershell
if (!(Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "⚠️ Đang tự động cài đặt Microsoft OpenJDK 21 qua winget..." -ForegroundColor Cyan
    winget install Microsoft.OpenJDK.21 --silent --accept-package-agreements --accept-source-agreements
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
}
```

#### 🔹 Đoạn 2: Tự động tải mã nguồn sạch từ GitHub và giải nén (Không cần cài Git)
Copy đoạn mã sau dán vào PowerShell và nhấn **Enter**:
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

---

### Cách 2: Chạy đúng 1 dòng lệnh duy nhất (Tự động hoàn toàn)
Copy đúng **1 dòng lệnh** sau dán vào PowerShell rồi nhấn **Enter**:
```powershell
if (!(Get-Command java -ErrorAction SilentlyContinue)) { Write-Host "⚠️ Đang cài đặt OpenJDK 21..." -ForegroundColor Cyan; winget install Microsoft.OpenJDK.21 --silent --accept-package-agreements --accept-source-agreements; $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User") }; if (Test-Path soa-auth-system-main) { Remove-Item -Recurse -Force soa-auth-system-main }; Invoke-WebRequest -Uri "https://github.com/quanzai123/soa-auth-system/archive/refs/heads/main.zip" -OutFile "soa.zip"; Expand-Archive -Path "soa.zip" -DestinationPath "." -Force; Set-Location "soa-auth-system-main"; .\run.bat
```

---

### Cách 3: Không cần gõ lệnh (Nhấp chuột)
1. Tải file ZIP trực tiếp: [Tải soa-auth-system-main.zip](https://github.com/quanzai123/soa-auth-system/archive/refs/heads/main.zip)
2. Chuột phải vào file vừa tải `soa-auth-system-main.zip` → Chọn **Extract All... (Giải nén tất cả)**.
3. Mở thư mục vừa giải nén → **Nhấp đúp chuột vào file `run.bat`** là hệ thống tự khởi động!

---

## 🍏 PHẦN 2: DÀNH CHO MÁY MACOS / LINUX (Sử dụng Terminal)

Mở ứng dụng **Terminal** và thực hiện:

### Cách 1: Chạy theo 2 đoạn mã tuần tự

#### 🔹 Đoạn 1: Tải mã nguồn sạch từ GitHub & chuyển vào thư mục dự án
```bash
rm -rf soa-auth-system && git clone https://github.com/quanzai123/soa-auth-system.git
cd soa-auth-system
```

#### 🔹 Đoạn 2: Cấp quyền thực thi & khởi động hệ thống
```bash
chmod +x mvnw run.sh start.command && ./run.sh
```

---

### Cách 2: Chạy đúng 1 dòng lệnh duy nhất
```bash
rm -rf soa-auth-system && git clone https://github.com/quanzai123/soa-auth-system.git && cd soa-auth-system && chmod +x mvnw run.sh start.command && ./run.sh
```

---

### Cách 3: Nhấp đúp chuột trên Mac
Mở thư mục dự án `soa-auth-system` và **nhấp đúp chuột vào file `start.command`**!

---

## 🌐 ĐỊA CHỈ TRUY CẬP HỆ THỐNG

Sau khi màn hình terminal hiển thị `Ready / Started SoaAuthSystemApplication`, mở trình duyệt truy cập:

| Tính Năng | Đường Dẫn URL | Mô Tả |
| :--- | :--- | :--- |
| **Cổng Xác Thực Web** | [http://localhost:8080](http://localhost:8080) | Trang chủ đăng nhập Google, quản lý hồ sơ và bảng điều khiển API |
| **Xem Trực Tiếp Database** | [http://localhost:8080/database.html](http://localhost:8080/database.html) | Trình giám sát bảng `users` & `user_identities` theo thời gian thực |
| **Tài Liệu Kỹ Thuật** | [http://localhost:8080/docs.html](http://localhost:8080/docs.html) | Tài liệu kiến trúc phân tầng, RFC 6749, JWT và xử lý lỗi |

> 💡 **Ghi chú về Database:** Hệ thống mặc định chạy trên **CSDL nhúng H2 In-Memory trong RAM**, không cần cài đặt SQL Server hay Docker. Mọi dữ liệu mẫu của nhóm tác giả đã được tự động nạp sẵn.

---

## 📮 HƯỚNG DẪN KIỂM THỬ POSTMAN
1. File kịch bản kiểm thử nằm ngay tại thư mục gốc: **`soa-auth-collection.json`**
2. Mở Postman → Chọn **Import** → Kéo thả file `soa-auth-collection.json` vào.
3. Chạy theo thứ tự từ Request `01` đến `10`:
   - Giai đoạn 1: Public APIs
   - Giai đoạn 2: Đồng bộ phiên làm việc từ Web (`04. Đồng bộ phiên đăng nhập từ Web`)
   - Giai đoạn 3: Private APIs (Xem hồ sơ, Xem liên kết Google, Xem Database)
   - Giai đoạn 4: Thu hồi Token vào Blacklist và xác thực chặn đứng `403 Forbidden`.
