#!/bin/bash

echo "============================================================"
echo "🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG NHANH DỰ ÁN"
echo "Sinh viên: Nguyễn Anh Quân (MSSV: 524H0122) - TDTU"
echo "============================================================"

# Cấp quyền thực thi cho mvnw nếu tải qua file ZIP
chmod +x mvnw 2>/dev/null || true

# Tự động tạo .env từ .env.example nếu chưa có
if [ ! -f .env ] && [ -f .env.example ]; then
    echo "ℹ️ Khởi tạo file cấu hình môi trường .env từ .env.example..."
    cp .env.example .env
fi

# Chế độ khởi động: Mặc định là h2 (không cần Docker)
MODE="${1:-h2}"

if [ "$MODE" = "docker" ] || [ "$MODE" = "sqlserver" ]; then
    echo "🐳 Đang khởi động SQL Server 2022 qua Docker..."
    if docker compose up -d 2>/dev/null; then
        echo "✅ SQL Server 2022 đã sẵn sàng."
        echo "🌟 Khởi động Backend với SQL Server..."
        echo "🔗 Ứng dụng chạy tại: http://localhost:8080"
        ./mvnw spring-boot:run
        exit 0
    else
        echo "⚠️ Không thể khởi động Docker (cổng bận hoặc Docker tắt). Chuyển sang CSDL H2 In-Memory..."
    fi
fi

echo "🌟 Khởi động với H2 In-Memory Database (Chạy siêu tốc, không cần Docker)..."
echo "🔗 Ứng dụng chạy tại: http://localhost:8080"
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
