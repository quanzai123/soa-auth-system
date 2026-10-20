@echo off
chcp 65001 > nul
echo ============================================================
echo 🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG NHANH DỰ ÁN (WINDOWS)
echo Sinh viên: Nguyễn Anh Quân (MSSV: 524H0122) - TDTU
echo ============================================================

REM Tự động tạo .env từ .env.example nếu chưa có
if not exist .env if exist .env.example (
    echo ℹ️ Khởi tạo file cấu hình môi trường .env từ .env.example...
    copy .env.example .env > nul
)

REM Kiểm tra Docker
docker info > nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Phát hiện Docker đang chạy. Khởi động SQL Server 2022...
    docker compose up -d
    timeout /t 3 > nul
    echo 🌟 Khởi động với SQL Server 2022...
    echo 🔗 Mở trình duyệt tại: http://localhost:8080
    start http://localhost:8080
    mvnw.cmd spring-boot:run
) else (
    echo ℹ️ Không phát hiện Docker. Khởi động với CSDL nhúng H2 In-Memory...
    echo 🔗 Mở trình duyệt tại: http://localhost:8080
    start http://localhost:8080
    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
)
