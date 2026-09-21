package com.soa.auth;

import com.soa.auth.dto.LinkedAccountDto;
import com.soa.auth.dto.UserProfileDto;
import com.soa.auth.entity.User;
import com.soa.auth.entity.UserIdentity;
import com.soa.auth.repository.UserIdentityRepository;
import com.soa.auth.repository.UserRepository;
import com.soa.auth.security.JwtTokenProvider;
import com.soa.auth.security.TokenBlacklistService;
import com.soa.auth.service.GoogleOAuthService;
import com.soa.auth.service.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SecurityAndAuthTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @BeforeEach
    void setUp() {
        userIdentityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Test JWT Token: Ký, giải mã và trích xuất đúng Claims")
    void testJwtTokenGenerationAndClaims() {
        String email = "52100001@student.tdtu.edu.vn";
        String role = "ROLE_USER";
        String accountType = "STUDENT";

        String token = jwtTokenProvider.generateAccessToken(email, role, accountType);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));

        Claims claims = jwtTokenProvider.getClaimsFromToken(token);
        assertEquals(email, claims.getSubject());
        assertEquals(role, claims.get("role"));
        assertEquals(accountType, claims.get("accountType"));
    }

    @Test
    @DisplayName("2. Test Token Blacklist: Đăng xuất xong token lập tức bị vô hiệu hóa")
    void testTokenBlacklist() {
        String email = "user@gmail.com";
        String token = jwtTokenProvider.generateAccessToken(email, "ROLE_USER", "PERSONAL");

        assertFalse(tokenBlacklistService.isBlacklisted(token));

        long expiry = jwtTokenProvider.getExpirationEpochMs(token);
        tokenBlacklistService.blacklistToken(token, expiry);

        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    @DisplayName("3. Test Google Auth URL: Chứa đúng endpoint và query params chuẩn OAuth 2.0")
    void testGoogleAuthUrlGeneration() {
        String url = googleOAuthService.buildAuthorizationUrl("http://localhost:8080/api/auth/google/callback",
                "test-state");
        assertNotNull(url);
        assertTrue(url.startsWith("https://accounts.google.com/o/oauth2/v2/auth"));
        assertTrue(url.contains("scope=openid") && url.contains("profile") && url.contains("email"));
        assertTrue(url.contains("response_type=code"));
    }

    @Test
    @DisplayName("4. Test Quản lý Liên kết & Hủy liên kết Google (Ràng buộc an toàn)")
    void testLinkedAccountsAndUnlinkGoogle() {
        // Tạo tài khoản sinh viên TDTU
        User user = User.builder()
                .email("student@student.tdtu.edu.vn")
                .fullName("Sinh Vien TDTU")
                .accountType("STUDENT")
                .role("ROLE_USER")
                .passwordHash(null) // Chưa có mật khẩu
                .build();
        user = userRepository.save(user);

        // Tạo liên kết Google
        UserIdentity identity = UserIdentity.builder()
                .user(user)
                .provider("GOOGLE")
                .providerUserId("google_sub_123456")
                .providerEmail("student@student.tdtu.edu.vn")
                .build();
        userIdentityRepository.save(identity);

        // Kiểm tra danh sách liên kết
        List<LinkedAccountDto> linked = userService.getLinkedAccounts(user.getEmail());
        assertEquals(1, linked.size());
        assertEquals("GOOGLE", linked.get(0).getProvider());
        assertEquals("google_sub_123456", linked.get(0).getProviderUserId());

        // TH1: Hủy liên kết khi CHƯA có mật khẩu dự phòng -> Bị chặn
        assertThrows(IllegalStateException.class, () -> {
            userService.unlinkGoogle("student@student.tdtu.edu.vn");
        });

        // TH2: Tạo mật khẩu dự phòng -> Hủy liên kết thành công
        userService.setPassword("student@student.tdtu.edu.vn", "Password123@");

        assertDoesNotThrow(() -> {
            userService.unlinkGoogle("student@student.tdtu.edu.vn");
        });

        // Xác nhận đã xóa liên kết trong DB
        List<LinkedAccountDto> remainingLinked = userService.getLinkedAccounts(user.getEmail());
        assertEquals(0, remainingLinked.size());

        // Tài khoản vẫn tồn tại với mật khẩu dự phòng
        UserProfileDto profile = userService.getProfile(user.getEmail());
        assertTrue(profile.isHasPassword());
    }
}
