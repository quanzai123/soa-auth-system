package com.soa.auth.controller;

import com.soa.auth.dto.ApiResponse;
import com.soa.auth.dto.AuthResponse;
import com.soa.auth.dto.RefreshTokenRequest;
import com.soa.auth.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final com.soa.auth.service.GoogleOAuthService googleOAuthService;

    // 🟢 PUBLIC API: Kiểm tra trạng thái cấu hình Google Client ID
    @GetMapping("/google/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGoogleConfig() {
        return ResponseEntity.ok(ApiResponse.ok("Trạng thái cấu hình Google OAuth", Map.of(
                "isConfigured", googleOAuthService.isConfigured(),
                "clientId", googleOAuthService.getClientId(),
                "redirectUri", googleOAuthService.getDefaultRedirectUri(),
                "hasSecret", googleOAuthService.hasClientSecret())));
    }

    // 🟢 PUBLIC API: Cập nhật Google Client ID và Secret trực tiếp từ giao diện web
    @PostMapping("/google/config")
    public ResponseEntity<ApiResponse<Void>> updateGoogleConfig(@RequestBody Map<String, String> body) {
        String clientId = body.get("clientId");
        String clientSecret = body.get("clientSecret");
        String redirectUri = body.get("redirectUri");
        if (clientId == null || clientId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Client ID không được để trống!"));
        }
        googleOAuthService.updateCredentials(clientId, clientSecret, redirectUri);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật Google Client ID & Secret thành công!"));
    }

    // 🟢 PUBLIC API: Lấy URL để chuyển hướng người dùng sang trang đăng nhập Google
    @GetMapping("/google/url")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGoogleLoginUrl(
            @RequestParam(required = false) String redirectUri,
            @RequestParam(required = false) String state) {
        String url = authService.getGoogleLoginUrl(redirectUri, state);
        return ResponseEntity.ok(ApiResponse.ok("Khởi tạo đường dẫn Google OAuth 2.0 thành công", Map.of("url", url)));
    }

    // 🟢 PUBLIC API: Nhận redirect từ trình duyệt Google và chuyển tiếp về trang
    // web kèm code
    @GetMapping("/google/callback")
    public void googleCallbackGet(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "error", required = false) String error,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        if (error != null) {
            response.sendRedirect(
                    "/?error=" + java.net.URLEncoder.encode(error, java.nio.charset.StandardCharsets.UTF_8));
            return;
        }
        if (code != null) {
            response.sendRedirect("/?code=" + java.net.URLEncoder.encode(code, java.nio.charset.StandardCharsets.UTF_8)
                    + "&viaCallback=true");
            return;
        }
        response.sendRedirect("/");
    }

    // 🟢 PUBLIC API: Nhận mã authorization code từ Google và trả về JWT
    @PostMapping("/google/callback")
    public ResponseEntity<ApiResponse<AuthResponse>> googleCallback(@RequestBody GoogleCallbackRequest request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu authorization code từ Google"));
        }
        AuthResponse response = authService.handleGoogleCallback(request.getCode(), request.getRedirectUri());
        return ResponseEntity.ok(ApiResponse.ok("Xác thực Google OAuth 2.0 thành công", response));
    }

    // 🟢 PUBLIC API: Cấp lại Access Token mới khi token cũ hết hạn
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu refresh token"));
        }
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Làm mới access token thành công", response));
    }

    // 🔒 PRIVATE API: Đăng xuất và đưa token vào Blacklist thu hồi
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Thiếu hoặc sai định dạng Authorization Bearer token"));
        }
        authService.logout(bearerToken);
        return ResponseEntity.ok(ApiResponse.ok("Đăng xuất thành công, token đã được đưa vào danh sách thu hồi"));
    }

    @Data
    public static class GoogleCallbackRequest {
        private String code;
        private String redirectUri;
    }
}
