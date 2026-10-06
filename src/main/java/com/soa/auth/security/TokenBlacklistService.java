package com.soa.auth.security;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Lưu token và thời điểm hết hạn
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    // Lưu thời điểm đăng xuất gần nhất của người dùng
    private final Map<String, Long> userLogoutTimes = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expiryEpochMs) {
        if (token != null) {
            blacklist.put(token, expiryEpochMs);
            cleanup();
        }
    }

    public void recordUserLogout(String email) {
        if (email != null) {
            userLogoutTimes.put(email, System.currentTimeMillis());
        }
    }

    public boolean isUserLoggedOutBefore(String email, long issuedAtEpochMs) {
        if (email == null) return false;
        Long logoutTime = userLogoutTimes.get(email);
        if (logoutTime == null) return false;
        return issuedAtEpochMs <= logoutTime;
    }

    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        Long expiry = blacklist.get(token);
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
