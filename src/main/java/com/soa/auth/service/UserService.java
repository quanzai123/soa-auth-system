package com.soa.auth.service;

import com.soa.auth.dto.LinkedAccountDto;
import com.soa.auth.dto.UpdateProfileRequest;
import com.soa.auth.dto.UserProfileDto;
import com.soa.auth.entity.User;
import com.soa.auth.entity.UserIdentity;
import com.soa.auth.repository.UserIdentityRepository;
import com.soa.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final GoogleOAuthService googleOAuthService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));

        List<LinkedAccountDto> linkedAccounts = userIdentityRepository.findByUserId(user.getId()).stream()
                .map(identity -> LinkedAccountDto.builder()
                        .id(identity.getId())
                        .provider(identity.getProvider())
                        .providerEmail(identity.getProviderEmail())
                        .providerUserId(identity.getProviderUserId())
                        .linkedAt(identity.getLinkedAt())
                        .build())
                .toList();

        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .accountType(user.getAccountType())
                .role(user.getRole())
                .hasPassword(user.getPasswordHash() != null && !user.getPasswordHash().isBlank())
                .linkedAccounts(linkedAccounts)
                .build();
    }

    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + email));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(request.getAvatarUrl().trim());
        }

        userRepository.save(user);
        return getProfile(email);
    }

    @Transactional
    public void setPassword(String email, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có độ dài tối thiểu 6 ký tự!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + email));

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        log.info("Người dùng {} đã thiết lập mật khẩu thành công.", email);
    }

    @Transactional(readOnly = true)
    public List<LinkedAccountDto> getLinkedAccounts(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + email));

        return userIdentityRepository.findByUserId(user.getId()).stream()
                .map(identity -> LinkedAccountDto.builder()
                        .id(identity.getId())
                        .provider(identity.getProvider())
                        .providerEmail(identity.getProviderEmail())
                        .providerUserId(identity.getProviderUserId())
                        .linkedAt(identity.getLinkedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void unlinkGoogle(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + email));

        // Ràng buộc bảo vệ: Bắt buộc user phải có password hoặc 1 hình thức đăng nhập
        // khác
        boolean hasPassword = user.getPasswordHash() != null && !user.getPasswordHash().isBlank();
        List<UserIdentity> identities = userIdentityRepository.findByUserId(user.getId());

        boolean hasOtherProvider = identities.stream().anyMatch(i -> !"GOOGLE".equalsIgnoreCase(i.getProvider()));

        if (!hasPassword && !hasOtherProvider) {
            throw new IllegalStateException(
                    "Cần thiết lập mật khẩu dự phòng trước khi hủy liên kết Google để tránh mất quyền truy cập tài khoản!");
        }

        List<UserIdentity> googleIdentities = identities.stream()
                .filter(i -> "GOOGLE".equalsIgnoreCase(i.getProvider()))
                .toList();

        if (googleIdentities.isEmpty()) {
            throw new IllegalArgumentException("Tài khoản chưa từng liên kết với Google!");
        }

        for (UserIdentity googleIdentity : googleIdentities) {
            // Gọi Google Revoke API để hủy quyền ứng dụng trên tài khoản Google của user
            if (googleIdentity.getAccessToken() != null && !googleIdentity.getAccessToken().isBlank()) {
                try {
                    googleOAuthService.revokeToken(googleIdentity.getAccessToken());
                } catch (Exception e) {
                    log.warn("Không thể thu hồi token phía Google (có thể token hết hạn hoặc simulated): {}",
                            e.getMessage());
                }
            }
            userIdentityRepository.delete(googleIdentity);
        }
        log.info("Đã hủy toàn bộ liên kết Google cho người dùng {}", email);
    }
}
