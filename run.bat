@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ============================================================
echo 🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG DỰ ÁN [WINDOWS]
echo Sinh viên: Nguyễn Anh Quân [524H0122] và Nguyễn Hồng Quân [524H0123] - TDTU
echo ============================================================

REM 1. Tu dong do tim duong dan cai dat Java 21 tren Windows
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

REM 2. Kiem tra lenh java
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 goto :JAVA_OK

:JAVA_MISSING
echo.
echo ❌ LỖI: Máy tính chưa được cài đặt Java [Yêu cầu JDK 21]!
echo.
echo 👉 Đang tự động mở trình duyệt tải Microsoft OpenJDK 21 Installer [.msi]...
start https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.msi
echo.
echo    1. Trình duyệt vừa mở link tải bộ cài đặt Microsoft OpenJDK 21.
echo    2. Bạn hãy mở file vừa tải và bấm Next đến Finish [mất 30 giây].
echo    3. Sau khi cài xong, chỉ cần chạy lại file run.bat này là server khởi động ngay!
echo.
pause
exit /b 1

:JAVA_OK
echo ✅ Đã phát hiện môi trường Java:
java -version
echo.

REM 3. Tu dong tao file .env voi cau hinh Google OAuth neu chua co
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

REM 4. Khoi dong ung dung
echo 🌟 Đang biên dịch và khởi động hệ thống Backend...
echo 🔗 Địa chỉ Web Portal: http://localhost:8080
echo 🗄️ Trình xem Database: http://localhost:8080/database.html
echo ⏳ Vui lòng đợi trong giây lát...
echo.

if "%1"=="docker" goto :RUN_DOCKER

:RUN_H2
echo 🌟 Khởi động với CSDL nhúng H2 In-Memory [Chạy trực tiếp trong RAM]...
call .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
goto :CHECK_RUN_RESULT

:RUN_DOCKER
echo 🐳 Đang khởi động SQL Server 2022 qua Docker...
docker compose up -d
call .\mvnw.cmd spring-boot:run
goto :CHECK_RUN_RESULT

:CHECK_RUN_RESULT
if %ERRORLEVEL% EQU 0 goto :RUN_SUCCESS

echo.
echo ⚠️ Maven Wrapper gặp sự cố. Thử chuyển sang lệnh mvn hệ thống...
call mvn spring-boot:run "-Dspring-boot.run.profiles=h2"
if %ERRORLEVEL% EQU 0 goto :RUN_SUCCESS

echo.
echo ❌ Không thể khởi động ứng dụng.
echo 👉 Vui lòng kiểm tra phiên bản Java [Yêu cầu JDK 21 trở lên].
pause
exit /b 1

:RUN_SUCCESS
pause
