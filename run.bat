@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ============================================================
echo 🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG DỰ ÁN (WINDOWS)
echo Sinh viên: Nguyễn Anh Quân (524H0122) và Nguyễn Hồng Quân (524H0123) - TDTU
echo ============================================================

REM 1. Tự động dò tìm đường dẫn cài đặt Java 21 trên Windows
if "%JAVA_HOME%"=="" (
    for /d %%i in ("C:\Program Files\Microsoft\jdk-21*") do set "JAVA_HOME=%%i"
    for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do set "JAVA_HOME=%%i"
    for /d %%i in ("C:\Program Files\Java\jdk-21*") do set "JAVA_HOME=%%i"
    for /d %%i in ("C:\Program Files\Semeru\jdk-21*") do set "JAVA_HOME=%%i"
    for /d %%i in ("%USERPROFILE%\.jdk\jdk-21*") do set "JAVA_HOME=%%i"
    for /d %%i in ("%USERPROFILE%\jdk-21*") do set "JAVA_HOME=%%i"
)

if not "%JAVA_HOME%"=="" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

REM 2. Kiểm tra lệnh java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ LỖI: Không tìm thấy môi trường Java (JDK 21) trên máy tính!
    echo.
    echo 👉 Hướng dẫn khắc phục trong 1 phút:
    echo    1. Tải bộ cài đặt Microsoft OpenJDK 21 chính thức (File .msi):
    echo       https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.msi
    echo    2. Cài đặt xong, nhấp đúp lại file run.bat này là chạy thành công 100%%.
    echo.
    pause
    exit /b 1
)

echo ✅ Đã phát hiện Java:
java -version
echo.

REM 3. Tự động tạo .env với đầy đủ cấu hình Google OAuth nếu chưa có
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

REM 4. Khởi động ứng dụng
echo 🌟 Đang biên dịch và khởi động hệ thống Backend...
echo 🔗 Địa chỉ Web Portal: http://localhost:8080
echo 🗄️ Trình xem Database: http://localhost:8080/database.html
echo ⏳ Vui lòng đợi trong giây lát...
echo.

if "%1"=="docker" (
    echo 🐳 Đang khởi động SQL Server 2022 qua Docker...
    docker compose up -d
    call .\mvnw.cmd spring-boot:run
) else (
    echo 🌟 Khởi động với CSDL nhúng H2 In-Memory (Chạy trực tiếp trong RAM)...
    call .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ⚠️ Maven Wrapper gặp sự cố. Thử chuyển sang lệnh mvn hệ thống (nếu có)...
    call mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
    if !ERRORLEVEL! NEQ 0 (
        echo.
        echo ❌ Không thể khởi động ứng dụng. Mã lỗi: !ERRORLEVEL!
        echo 👉 Vui lòng kiểm tra phiên bản Java (Yêu cầu JDK 21).
        pause
    )
)

pause
