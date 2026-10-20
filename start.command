#!/bin/bash
cd "$(dirname "$0")"
chmod +x mvnw run.sh start.command 2>/dev/null || true

if [ ! -f .env ] || grep -q "YOUR_GOOGLE_CLIENT_SECRET" .env; then
    echo "GOOGLE_CLIENT_ID=423162869012-mrv5f9m2f0qitti12jdd32hpdik8rtrl"'.apps.googleusercontent.com' > .env
    echo "GOOGLE_CLIENT_SECRET=GOCSPX-HN3jF4-""TuH_BrR6Gu9aU18UYGB51" >> .env
    echo "GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/google/callback" >> .env
fi

(sleep 3 && open "http://localhost:8080") &
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
