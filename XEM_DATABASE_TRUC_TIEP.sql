-- ====================================================================
-- SCRIPT XEM TOÀN BỘ DỮ LIỆU SQL SERVER (CHẠY TRỰC TIẾP TRÊN DBEAVER)
-- ====================================================================

-- 1. Chuyển sang đúng database google_login_db
USE google_login_db;
GO

-- 2. Xem toàn bộ hồ sơ người dùng trong hệ thống
SELECT 
    id AS [ID], 
    email AS [Email], 
    full_name AS [Họ và Tên], 
    account_type AS [Loại TK], 
    role AS [Quyền], 
    password_hash AS [Mật khẩu Hash (BCrypt)], 
    created_at AS [Ngày Tạo]
FROM users;
GO

-- 3. Xem danh sách liên kết định danh Google OAuth 2.0
SELECT 
    id AS [ID], 
    user_id AS [User ID], 
    provider AS [Nhà Cung Cấp], 
    provider_user_id AS [Google Subject ID (sub)], 
    provider_email AS [Email Google], 
    linked_at AS [Thời Điểm Liên Kết]
FROM user_identities;
GO

