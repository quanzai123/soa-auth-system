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

        // 4. Ký và sinh bộ đôi JWT (Access Token & Refresh Token)
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

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng cho refresh token này."));

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

    public AuthResponse getLatestSession() {
        AuthResponse current = latestSession.get();
        if (current != null && !tokenBlacklistService.isBlacklisted(current.getAccessToken())) {
            return current;
        }
        // Fallback: Tìm tài khoản gần nhất trong DB để cấp token sẵn sàng
        java.util.List<User> users = userRepository.findAll();
        if (!users.isEmpty()) {
            User latestUser = users.get(users.size() - 1);
            String accessToken = jwtTokenProvider.generateAccessToken(latestUser.getEmail(), latestUser.getRole(), latestUser.getAccountType());
            String refreshToken = jwtTokenProvider.generateRefreshToken(latestUser.getEmail());
            UserProfileDto profile = userService.getProfile(latestUser.getEmail());
            AuthResponse fallback = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                    .user(profile)
                    .build();
            latestSession.set(fallback);
            return fallback;
        }
        return null;
    }

    public AuthResponse getDemoNoPasswordSession() {
        User user = userRepository.findByEmail("hieuhieu5933@gmail.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getPasswordHash() == null)
                        .findFirst()
                        .orElse(null));
        if (user != null) {
            // Đảm bảo user này luôn có Google identity mẫu
            if (userIdentityRepository.findByUserId(user.getId()).isEmpty()) {
                userIdentityRepository.save(UserIdentity.builder()
                        .user(user)
                        .provider("GOOGLE")
                        .providerUserId("106790612631009028951")
                        .providerEmail(user.getEmail())
                        .accessToken("ya29.demo_test_token")
                        .build());
            }
            String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole(), user.getAccountType());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
            UserProfileDto profile = userService.getProfile(user.getEmail());
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationMs() / 1000)
                    .user(profile)
                    .build();
        }
        return null;
    }

    public void logout(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                long expiration = jwtTokenProvider.getExpirationEpochMs(token);
                tokenBlacklistService.blacklistToken(token, expiration);
                latestSession.set(null);
                log.info("Đã đưa token vào Blacklist thu hồi thành công và xóa session hiện tại.");
            }
        }
    }
}
