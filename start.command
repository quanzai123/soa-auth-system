#!/bin/bash
cd "$(dirname "$0")"
chmod +x mvnw run.sh start.command 2>/dev/null || true

# Tự động mở trình duyệt sau 3 giây khi server sẵn sàng
(sleep 3 && open "http://localhost:8080") &

# Chạy Spring Boot ở chế độ H2 In-Memory (Siêu tốc, không cần Docker)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
