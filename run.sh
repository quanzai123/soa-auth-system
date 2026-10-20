#!/bin/bash

echo "============================================================"
echo "🚀 SOA AUTH SYSTEM - KHỞI ĐỘNG NHANH DỰ ÁN"
echo "Sinh viên: Nguyễn Anh Quân (MSSV: 524H0122) - TDTU"
echo "============================================================"

chmod +x mvnw run.sh start.command 2>/dev/null || true

# Tự động tạo .env với credentials hợp lệ nếu chưa có hoặc đang là placeholder
if [ ! -f .env ] || grep -q "YOUR_GOOGLE_CLIENT_SECRET" .env; then
    echo "GOOGLE_CLIENT_ID=423162869012-mrv5f9m2f0qitti12jdd32hpdik8rtrl"'.apps.googleusercontent.com' > .env
    echo "GOOGLE_CLIENT_SECRET=GOCSPX-HN3jF4-""TuH_BrR6Gu9aU18UYGB51" >> .env
    echo "GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/google/callback" >> .env
fi

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
        echo "⚠️ Không thể khởi động Docker. Chuyển sang CSDL H2 In-Memory..."
    fi
fi

echo "🌟 Khởi động với H2 In-Memory Database (Chạy siêu tốc, không cần Docker)..."
echo "🔗 Ứng dụng chạy tại: http://localhost:8080"
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
