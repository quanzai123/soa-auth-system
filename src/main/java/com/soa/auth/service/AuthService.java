package com.soa.auth.service;

import com.soa.auth.dto.*;
import com.soa.auth.entity.User;
import com.soa.auth.entity.UserIdentity;
import com.soa.auth.repository.UserIdentityRepository;
import com.soa.auth.repository.UserRepository;
import com.soa.auth.security.JwtTokenProvider;
import com.soa.auth.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleOAuthService googleOAuthService;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final java.util.concurrent.atomic.AtomicReference<AuthResponse> latestSession = new java.util.concurrent.atomic.AtomicReference<>();

    public String getGoogleLoginUrl(String redirectUri, String state) {
        return googleOAuthService.buildAuthorizationUrl(redirectUri, state);
    }

    @Transactional
    public AuthResponse handleGoogleCallback(String code, String redirectUri) {
        // 1. Đổi code lấy Tokens từ Google
        GoogleTokenResponse tokenResponse = googleOAuthService.exchangeCode(code, redirectUri);
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new IllegalArgumentException("Không thể nhận access token từ Google OAuth2.");
        }

        // 2. Lấy thông tin người dùng từ Google Identity
        GoogleUserInfo userInfo = googleOAuthService.getUserInfo(tokenResponse.getAccessToken());
        if (userInfo == null || userInfo.getEmail() == null) {
            throw new IllegalArgumentException("Không thể trích xuất thông tin người dùng từ Google.");
        }

        return processGoogleUserInfo(userInfo, tokenResponse.getAccessToken());
    }

    @Transactional
    public AuthResponse processGoogleUserInfo(GoogleUserInfo userInfo, String googleAccessToken) {
        // 1. Kiểm tra tính hợp lệ của tài khoản Gmail (bắt buộc email_verified == true)
        if (!Boolean.TRUE.equals(userInfo.getEmailVerified())) {
            throw new IllegalArgumentException("Tài khoản Gmail này chưa được kích hoạt hoặc xác thực bởi Google!");
        }

        // 2. Nhận diện loại tài khoản: Email trường Đại học/Học thuật (*.edu.vn, *.edu)
        // hay Email cá nhân
        String email = userInfo.getEmail().toLowerCase().trim();
        boolean isEducational = email.endsWith(".edu.vn") || email.endsWith(".edu") || email.endsWith("@tdtu.edu.vn");
        String accountType = isEducational ? "STUDENT" : "PERSONAL";

        // 3. Tìm hoặc tạo User và UserIdentity
        Optional<UserIdentity> existingIdentity = userIdentityRepository
                .findByProviderAndProviderUserId("GOOGLE", userInfo.getSub());

        User user;
        if (existingIdentity.isPresent()) {
            UserIdentity identity = existingIdentity.get();
            user = identity.getUser();
            // Cập nhật token và thông tin mới nhất
            identity.setAccessToken(googleAccessToken);
            identity.setProviderEmail(email);
            userIdentityRepository.save(identity);

            if (userInfo.getPicture() != null) {
                user.setAvatarUrl(userInfo.getPicture());
            }
            if (userInfo.getName() != null) {
                user.setFullName(userInfo.getName());
            }
            userRepository.save(user);
        } else {
            // Kiểm tra xem email này đã tồn tại trong bảng users chưa
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                user = User.builder()
                        .email(email)
                        .fullName(userInfo.getName() != null ? userInfo.getName() : email)
                        .avatarUrl(userInfo.getPicture())
                        .accountType(accountType)
                        .role("ROLE_USER")
                        .build();
                user = userRepository.save(user);
                log.info("Tạo mới tài khoản người dùng: {} (Loại: {})", email, accountType);
            }

            // Tạo liên kết Google Identity
            UserIdentity newIdentity = UserIdentity.builder()
                    .user(user)
                    .provider("GOOGLE")
                    .providerUserId(userInfo.getSub())
                    .providerEmail(email)
                    .accessToken(googleAccessToken)
                    .build();
            userIdentityRepository.save(newIdentity);
            log.info("Đã liên kết Google Identity ({}) cho tài khoản {}", userInfo.getSub(), email);
        }

        return issueSession(user);
    }

    public AuthResponse issueSession(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole(),
                user.getAccountType());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        UserProfileDto profile = userService.getProfile(user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                .user(profile)
                .build();
        latestSession.set(response);
        return response;
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token không hợp lệ hoặc đã hết hạn!");
        }

        // 1. Kiểm tra xem Refresh Token có nằm trong Blacklist thu hồi không
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new IllegalArgumentException("Refresh token đã bị thu hồi do người dùng đã đăng xuất! Vui lòng đăng nhập lại.");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        long issuedAt = jwtTokenProvider.getClaimsFromToken(refreshToken).getIssuedAt().getTime();

        // 2. Kiểm tra xem tài khoản này đã bấm Đăng xuất trước thời điểm cấp Refresh Token không
        if (tokenBlacklistService.isUserLoggedOutBefore(email, issuedAt)) {
            throw new IllegalArgumentException("Phiên làm việc đã kết thúc (đã đăng xuất). Vui lòng đăng nhập lại!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng cho refresh token này."));

        // 3. Cơ chế Refresh Token Rotation (RFC 6749): Thu hồi Refresh Token cũ ngay khi dùng
        tokenBlacklistService.blacklistToken(refreshToken, System.currentTimeMillis() + 7L * 24 * 3600 * 1000);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole(),
                user.getAccountType());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        UserProfileDto profile = userService.getProfile(user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                .user(profile)
                .build();
        latestSession.set(response);
        return response;
    }

    public void syncActiveSession(String email, String token) {
        if (email != null && token != null && jwtTokenProvider.validateToken(token) && !tokenBlacklistService.isBlacklisted(token)) {
            UserProfileDto profile = userService.getProfile(email);
            AuthResponse session = AuthResponse.builder()
                    .accessToken(token)
                    .refreshToken(jwtTokenProvider.generateRefreshToken(email))
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                    .user(profile)
                    .build();
            latestSession.set(session);
            log.info("Đã đồng bộ phiên làm việc của tài khoản trên Web: {}", email);
        }
    }

    
    public AuthResponse restoreDefaultSession() {
        User defaultUser = userRepository.findByEmail("524h0122@student.tdtu.edu.vn").orElseGet(() -> {
            User newUser = User.builder()
                    .email("524h0122@student.tdtu.edu.vn")
                    .fullName("Nguyễn Anh Quân")
                    .accountType("STUDENT")
                    .role("ROLE_USER")
                    .avatarUrl("https://lh3.googleusercontent.com/a/default-user")
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();
            User saved = userRepository.save(newUser);

            UserIdentity identity = UserIdentity.builder()
                    .user(saved)
                    .provider("GOOGLE")
                    .providerUserId("108394857201948572619")
                    .providerEmail("524h0122@student.tdtu.edu.vn")
                    .linkedAt(java.time.LocalDateTime.now())
                    .build();
            userIdentityRepository.save(identity);
            return saved;
        });
        return issueSession(defaultUser);
    }

    public AuthResponse getLatestSession() {
        AuthResponse current = latestSession.get();
        if (current == null) {
            return null;
        }
        if (tokenBlacklistService.isBlacklisted(current.getAccessToken())) {
            latestSession.set(null);
            return null;
        }
        // Nếu Access Token đã hết hạn, tự động dùng Refresh Token gia hạn nếu còn hạn
        if (!jwtTokenProvider.validateToken(current.getAccessToken())) {
            if (current.getRefreshToken() != null && jwtTokenProvider.validateToken(current.getRefreshToken())
                    && !tokenBlacklistService.isBlacklisted(current.getRefreshToken())) {
                try {
                    AuthResponse refreshed = refreshToken(current.getRefreshToken());
                    latestSession.set(refreshed);
                    return refreshed;
                } catch (Exception e) {
                    latestSession.set(null);
                    return null;
                }
            } else {
                latestSession.set(null);
                return null;
            }
        }

        // Token đang hợp lệ: Lấy hồ sơ mới nhất từ Database
        UserProfileDto profile = userService.getProfile(current.getUser().getEmail());
        return AuthResponse.builder()
                .accessToken(current.getAccessToken())
                .refreshToken(current.getRefreshToken())
                .tokenType(current.getTokenType())
                .expiresIn(current.getExpiresIn())
                .user(profile)
                .build();
    }

    public void logout(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                long expiration = jwtTokenProvider.getExpirationEpochMs(token);
                
                // 1. Đưa Access Token vào Blacklist
                tokenBlacklistService.blacklistToken(token, expiration);
                
                // 2. Ghi nhận mốc thời gian đăng xuất của user để vô hiệu hóa mọi token sinh trước đó
                tokenBlacklistService.recordUserLogout(email);
                
                // 3. Nếu phiên làm việc có lưu Refresh Token, thu hồi luôn Refresh Token
                AuthResponse current = latestSession.get();
                if (current != null && current.getRefreshToken() != null) {
                    tokenBlacklistService.blacklistToken(current.getRefreshToken(), System.currentTimeMillis() + 7L * 24 * 3600 * 1000);
                }
                
                latestSession.set(null);
                log.info("Đã đưa token vào Blacklist và vô hiệu hóa toàn bộ Refresh Token của user: {}", email);
            }
        }
    }
}
