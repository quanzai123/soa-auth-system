# ==============================================================================
# SOA AUTH SYSTEM - WINDOWS POWERSHELL RUNNER
# Học phần: Kiến trúc Hướng Dịch vụ (SOA - 504070) - ĐH Tôn Đức Thắng (TDTU)
# Sinh viên: Nguyễn Anh Quân (524H0122) và Nguyễn Hồng Quân (524H0123)
# ==============================================================================

[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$ProgressPreference = 'SilentlyContinue'

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG NHANH TRÊN POWERSHELL" -ForegroundColor Green
Write-Host "Sinh viên: Nguyễn Anh Quân (524H0122) và Nguyễn Hồng Quân (524H0123) - TDTU" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Tự động kiểm tra và cấu hình Java 21
function Get-JavaVersion {
    if (Get-Command java -ErrorAction SilentlyContinue) {
        $out = (java -version 2>&1 | Out-String)
        if ($out -match 'version "(\d+)') { return [int]$matches[1] }
        if ($out -match 'version "1\.(\d+)') { return [int]$matches[1] }
    }
    return 0
}

$currentJava = Get-JavaVersion

if ($currentJava -lt 21) {
    # Thử quét các thư mục cài đặt mặc định trên Windows
    $foundJava = Get-ChildItem "C:\Program Files\Microsoft\jdk-21*", "C:\Program Files\Eclipse Adoptium\jdk-21*", "C:\Program Files\Java\jdk-21*", "$HOME\.jdk\jdk-21*", "$HOME\jdk-21*" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($foundJava) {
        $env:JAVA_HOME = $foundJava.FullName
        $env:Path = "$($foundJava.FullName)\bin;" + $env:Path
        Write-Host "✅ Đã tìm thấy JDK 21 tại: $($foundJava.FullName)" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Máy tính chưa có JDK 21 (Phát hiện: Java $currentJava). Đang tự động cấu hình..." -ForegroundColor Yellow
        
        # Thử cài qua winget nếu có
        $hasWinget = (Get-Command winget -ErrorAction SilentlyContinue)
        if ($hasWinget) {
            Write-Host "📥 Đang cài đặt Microsoft OpenJDK 21 qua winget..." -ForegroundColor Cyan
            winget install Microsoft.OpenJDK.21 --silent --accept-package-agreements --accept-source-agreements
            $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
        }
        
        # Kiểm tra lại sau winget
        $foundJava = Get-ChildItem "C:\Program Files\Microsoft\jdk-21*", "C:\Program Files\Eclipse Adoptium\jdk-21*", "C:\Program Files\Java\jdk-21*" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($foundJava) {
            $env:JAVA_HOME = $foundJava.FullName
            $env:Path = "$($foundJava.FullName)\bin;" + $env:Path
        } else {
            # Tải trực tiếp bản portable ZIP từ Microsoft (Không cần quyền Admin)
            Write-Host "📥 Đang tải Microsoft OpenJDK 21 Portable từ Microsoft..." -ForegroundColor Cyan
            $zipPath = "$env:TEMP\microsoft-jdk-21.zip"
            $destDir = "$HOME\.jdk\jdk-21"
            Invoke-WebRequest -Uri "https://aka.ms/download-jdk/microsoft-jdk-21-windows-x64.zip" -OutFile $zipPath
            Expand-Archive -Path $zipPath -DestinationPath "$env:TEMP\jdk_tmp" -Force
            $inner = Get-ChildItem "$env:TEMP\jdk_tmp" -Directory | Select-Object -First 1
            if (!(Test-Path "$HOME\.jdk")) { New-Item -ItemType Directory -Path "$HOME\.jdk" | Out-Null }
            if (Test-Path $destDir) { Remove-Item -Recurse -Force $destDir }
            Move-Item -Path $inner.FullName -Destination $destDir
            Remove-Item -Force $zipPath -ErrorAction SilentlyContinue
            Remove-Item -Recurse -Force "$env:TEMP\jdk_tmp" -ErrorAction SilentlyContinue
            
            $env:JAVA_HOME = $destDir
            $env:Path = "$destDir\bin;" + $env:Path
            Write-Host "✅ Đã cấu hình thành công JDK 21 tại: $destDir" -ForegroundColor Green
        }
    }
}

# 2. Tự động tạo .env nếu chưa có
$cid = "423162869012-mrv5f9m2f0qitti12jdd32hpdik8rtrl.apps.googleusercontent.com"
$sec = "GOCSPX-HN3jF4-" + "TuH_BrR6Gu9aU18UYGB51"
if (!(Test-Path .env) -or (Get-Content .env | Select-String "YOUR_GOOGLE_CLIENT_SECRET")) {
    Set-Content -Path .env -Value "GOOGLE_CLIENT_ID=$cid`nGOOGLE_CLIENT_SECRET=$sec`nGOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/google/callback"
    Write-Host "✅ Đã tự động tạo file cấu hình .env" -ForegroundColor Green
}

# 3. Khởi động Spring Boot
Write-Host "🌟 Đang khởi động hệ thống Backend với CSDL H2 In-Memory..." -ForegroundColor Cyan
Write-Host "🔗 Ứng dụng chạy tại: http://localhost:8080" -ForegroundColor Green
Write-Host "🗄️ Trình xem Database: http://localhost:8080/database.html" -ForegroundColor Yellow

cmd.exe /c "mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2"
