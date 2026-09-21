package com.soa.auth.controller;

import com.soa.auth.dto.*;
import com.soa.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 🔒 PRIVATE API: Xem thông tin tài khoản hiện tại
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUserProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileDto profile = userService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin tài khoản thành công", profile));
    }

    // 🔒 PRIVATE API: Cập nhật họ tên, ảnh đại diện
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        UserProfileDto profile = userService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin thành công", profile));
    }

    // 🔒 PRIVATE API: Thiết lập mật khẩu dự phòng (bắt buộc trước khi hủy liên kết Google)
    @PostMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> setPassword(
            Authentication authentication,
            @RequestBody SetPasswordRequest request) {
        String email = authentication.getName();
        userService.setPassword(email, request.getPassword());
        return ResponseEntity.ok(ApiResponse.ok("Thiết lập mật khẩu dự phòng thành công"));
    }

    // 🔒 PRIVATE API: Kiểm tra các nhà cung cấp/tài khoản đang liên kết
    @GetMapping("/me/linked-accounts")
    public ResponseEntity<ApiResponse<List<LinkedAccountDto>>> getLinkedAccounts(Authentication authentication) {
        String email = authentication.getName();
        List<LinkedAccountDto> accounts = userService.getLinkedAccounts(email);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách liên kết tài khoản thành công", accounts));
    }

    // 🔒 PRIVATE API: Hủy liên kết Google và thu hồi Token phía Google
    @DeleteMapping("/me/linked-accounts/google")
    public ResponseEntity<ApiResponse<Void>> unlinkGoogle(Authentication authentication) {
        String email = authentication.getName();
        userService.unlinkGoogle(email);
        return ResponseEntity.ok(ApiResponse.ok("Đã hủy liên kết tài khoản Google và thu hồi quyền truy cập thành công"));
    }
}

