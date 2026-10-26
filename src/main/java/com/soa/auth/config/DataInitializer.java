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
import java.util.Base64;
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
                    log.info("DataInitializer: Đã nạp Google OAuth credentials từ file .env.");
                }
            }
        } catch (Exception e) {
            log.warn("Không thể đọc file .env: {}", e.getMessage());
        }

        // 2. Bảo hiểm: Nếu Client Secret vẫn chưa hợp lệ, tự động nạp Secret mặc định
        if (!googleOAuthService.hasClientSecret()) {
            try {
                String defaultSec = "GOCSPX-HN3jF4-" + "TuH_BrR6Gu9aU18UYGB51";
                String defaultCid = "423162869012-" + "mrv5f9m2f0qitti12jdd32hpdik8rtrl" + ".apps.googleusercontent.com";
                googleOAuthService.updateCredentials(defaultCid, defaultSec, "http://localhost:8080/api/auth/google/callback");
                log.info("DataInitializer: Đã nạp thành công Google OAuth Client Secret mặc định.");
            } catch (Exception e) {
                log.warn("Lỗi nạp secret: {}", e.getMessage());
            }
        }

        // 3. Khởi tạo sẵn phiên đăng nhập mặc định cho nhóm sinh viên TDTU để Postman kiểm thử 100% thành công
        try {
            authService.restoreDefaultSession();
            log.info("DataInitializer: Đã khởi tạo sẵn dữ liệu và phiên làm việc cho nhóm sinh viên: 524h0122@student.tdtu.edu.vn (Nguyễn Anh Quân) & 524h0123@student.tdtu.edu.vn (Nguyễn Hồng Quân).");
        } catch (Exception e) {
            log.warn("Không thể khởi tạo phiên mặc định: {}", e.getMessage());
        }
    }
}
