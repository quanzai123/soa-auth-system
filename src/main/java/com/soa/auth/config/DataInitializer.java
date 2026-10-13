package com.soa.auth.config;

import com.soa.auth.entity.User;
import com.soa.auth.entity.UserIdentity;
import com.soa.auth.repository.UserIdentityRepository;
import com.soa.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final com.soa.auth.service.GoogleOAuthService googleOAuthService;

        @Override
        public void run(String... args) {
                // Tự động nạp Google OAuth credentials từ file .env cục bộ nếu có
                try {
                        java.io.File envFile = new java.io.File(".env");
                        if (envFile.exists()) {
                                java.util.List<String> lines = java.nio.file.Files.readAllLines(envFile.toPath());
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
        }
}
