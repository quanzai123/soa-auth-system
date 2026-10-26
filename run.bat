@echo off
chcp 65001 > nul
echo ============================================================
echo 🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG NHANH DỰ ÁN (WINDOWS)
echo Sinh viên: Nguyễn Anh Quân (524H0122) và Nguyễn Hồng Quân (524H0123) - TDTU
echo ============================================================

REM 1. Kiểm tra Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if "%JAVA_HOME%"=="" (
        echo ❌ LỖI: Máy tính chưa cài Java hoặc chưa cấu hình biến môi trường JAVA_HOME!
        echo 👉 Vui lòng tải và cài đặt JDK 17 hoặc JDK 21 từ: https://www.oracle.com/java/technologies/downloads/
        pause
        exit /b 1
    )
)

REM 2. Tự động tạo .env với đầy đủ cấu hình Google OAuth nếu chưa có
set P1=GOCSPX-HN3jF4-
set P2=TuH_BrR6Gu9aU18UYGB51
set CID_P1=423162869012-mrv5f9m2f0qitti12jdd32hpdik8rtrl
set CID_P2=.apps.googleusercontent.com

findstr /C:"YOUR_GOOGLE_CLIENT_SECRET" .env >nul 2>&1
if %ERRORLEVEL% EQU 0 del .env

if not exist .env (
    echo ℹ️ Khởi tạo file cấu hình .env tự động...
    echo GOOGLE_CLIENT_ID=%CID_P1%%CID_P2%> .env
    echo GOOGLE_CLIENT_SECRET=%P1%%P2%>> .env
    echo GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/google/callback>> .env
)

REM 3. Khởi động ứng dụng
if "%1"=="docker" (
    echo 🐳 Đang khởi động SQL Server 2022 qua Docker...
    docker compose up -d
    echo 🌟 Khởi động Backend với SQL Server...
    start /b cmd /c "timeout /t 6 >nul && start http://localhost:8080"
    mvnw.cmd spring-boot:run
) else (
    echo 🌟 Khởi động với CSDL nhúng H2 In-Memory (Siêu tốc, không cần Docker)...
    echo 🔗 Trình duyệt sẽ tự động mở http://localhost:8080 sau vài giây...
    start /b cmd /c "timeout /t 6 >nul && start http://localhost:8080"
    mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
)

pause
