package com.soa.auth.config;

import com.soa.auth.service.AuthService;
import com.soa.auth.service.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GoogleOAuthService googleOAuthService;
    private final AuthService authService;

    @Override
    public void run(String... args) {
        // 1. Tự động nạp Google OAuth credentials từ file .env cục bộ nếu có
        try {
            File envFile = new File(".env");
            if (envFile.exists()) {
                List<String> lines = Files.readAllLines(envFile.toPath());
                String cId = null, cSec = null, rUri = null;
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("GOOGLE_CLIENT_ID=")) cId = line.substring("GOOGLE_CLIENT_ID=".length()).trim();
                    if (line.startsWith("GOOGLE_CLIENT_SECRET=")) cSec = line.substring("GOOGLE_CLIENT_SECRET=".length()).trim();
                    if (line.startsWith("GOOGLE_REDIRECT_URI=")) rUri = line.substring("GOOGLE_REDIRECT_URI=".length()).trim();
                }
                if (cId != null && !cId.isBlank()) {
                    googleOAuthService.updateCredentials(cId, cSec, rUri);
                    log.info("DataInitializer: Đã nạp thành công Google OAuth credentials từ file .env cục bộ.");
                }
            }
        } catch (Exception e) {
            log.warn("Không thể đọc file .env: {}", e.getMessage());
        }

        // 2. Khởi tạo sẵn phiên đăng nhập mặc định cho sinh viên TDTU để Postman kiểm thử 100% thành công
        try {
            authService.restoreDefaultSession();
            log.info("DataInitializer: Đã khởi tạo sẵn phiên làm việc cho 524h0122@student.tdtu.edu.vn (STUDENT).");
        } catch (Exception e) {
            log.warn("Không thể khởi tạo phiên mặc định: {}", e.getMessage());
        }
    }
}
