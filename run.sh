#!/bin/bash
set -e

echo "============================================================"
echo "🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG NHANH DỰ ÁN"
echo "Sinh viên: Nguyễn Anh Quân (MSSV: 524H0122) - TDTU"
echo "============================================================"

# Tự động tạo .env từ .env.example nếu chưa có
if [ ! -f .env ] && [ -f .env.example ]; then
    echo "ℹ️ Khởi tạo file cấu hình môi trường .env từ .env.example..."
    cp .env.example .env
fi

# Kiểm tra Docker
USE_H2=true
if command -v docker &> /dev/null; then
    if docker info &> /dev/null; then
        echo "✅ Phát hiện Docker đang chạy. Khởi động SQL Server 2022..."
        docker compose up -d
        USE_H2=false
        sleep 3
    else
        echo "ℹ️ Docker chưa được bật. Chuyển sang chế độ CSDL nhúng H2 (In-Memory)..."
    fi
else
    echo "ℹ️ Không tìm thấy Docker. Chuyển sang chế độ CSDL nhúng H2 (In-Memory)..."
fi

if [ "$USE_H2" = true ]; then
    echo "🌟 Khởi động với H2 In-Memory Database (Không cần cài SQL Server)..."
    echo "🔗 Ứng dụng chạy tại: http://localhost:8080"
    ./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
else
    echo "🌟 Khởi động với SQL Server 2022..."
    echo "🔗 Ứng dụng chạy tại: http://localhost:8080"
    ./mvnw spring-boot:run
fi
