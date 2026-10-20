@echo off
chcp 65001 > nul
echo ============================================================
echo 🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG NHANH DỰ ÁN (WINDOWS)
echo Sinh viên: Nguyễn Anh Quân (MSSV: 524H0122) - TDTU
echo ============================================================

if not exist .env if exist .env.example (
    echo ℹ️ Khởi tạo file cấu hình môi trường .env từ .env.example...
    copy .env.example .env > nul
)

if "%1"=="docker" (
    echo 🐳 Đang khởi động SQL Server 2022 qua Docker...
    docker compose up -d
    echo 🌟 Khởi động với SQL Server 2022...
    start http://localhost:8080
    mvnw.cmd spring-boot:run
) else (
    echo 🌟 Khởi động với CSDL nhúng H2 In-Memory (Siêu tốc, không cần Docker)...
    start http://localhost:8080
    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
)
