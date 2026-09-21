package com.soa.auth.controller;

import com.soa.auth.dto.ApiResponse;
import com.soa.auth.entity.User;
import com.soa.auth.entity.UserIdentity;
import com.soa.auth.repository.UserIdentityRepository;
import com.soa.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/database-viewer")
@RequiredArgsConstructor
public class DatabaseViewerController {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDatabaseSnapshot() {
        List<Map<String, Object>> userList = userRepository.findAll().stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("email", u.getEmail());
            map.put("fullName", u.getFullName());
            map.put("avatarUrl", u.getAvatarUrl());
            map.put("accountType", u.getAccountType());
            map.put("role", u.getRole());
            map.put("passwordHash", u.getPasswordHash() != null ? "$2a$10$***BCRYPT_MASKED***" : null);
            map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            map.put("updatedAt", u.getUpdatedAt() != null ? u.getUpdatedAt().toString() : null);
            return map;
        }).toList();

        List<Map<String, Object>> identityList = userIdentityRepository.findAll().stream().map(i -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", i.getId());
            map.put("user", Map.of("id", i.getUser().getId()));
            map.put("provider", i.getProvider());
            map.put("providerUserId", i.getProviderUserId());
            map.put("providerEmail", i.getProviderEmail());
            map.put("accessToken",
                    i.getAccessToken() != null
                            ? i.getAccessToken().substring(0, Math.min(10, i.getAccessToken().length())) + "...[MASKED]"
                            : null);
            map.put("linkedAt", i.getLinkedAt() != null ? i.getLinkedAt().toString() : null);
            return map;
        }).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("database", "google_login_db");
        data.put("host", "localhost:1433 (Microsoft SQL Server 2022 Docker)");
        data.put("totalUsers", userList.size());
        data.put("totalIdentities", identityList.size());
        data.put("users", userList);
        data.put("identities", identityList);

        return ResponseEntity
                .ok(ApiResponse.ok("Dữ liệu trực tiếp từ SQL Server (Đã che giấu thông tin nhạy cảm)", data));
    }
}
