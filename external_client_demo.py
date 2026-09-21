#!/usr/bin/env python3
# ==============================================================================
# DEMO: ỨNG DỤNG BÊN THỨ 3 (THIRD-PARTY CLIENT) GỌI VÀO SOA AUTH GATEWAY
# Mô phỏng một ứng dụng bên ngoài (Mobile App, Microservice khác) kết nối API
# ==============================================================================

import json
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8080"
TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI1MjRoMDEyMkBzdHVkZW50LnRkdHUuZWR1LnZuIiwicm9sZSI6IlJPTEVfVVNFUiIsImFjY291bnRUeXBlIjoiU1RVREVOVCIsImp0aSI6IjdkYTM1MmI4LTdkOWQtNDNhYy1iMDBiLTI1MjU5N2Y1Yjg2OSIsImlhdCI6MTc4OTk0OTcyOSwiZXhwIjoxNzg5OTUwNjI5fQ.Em88A3uE-TYE4KfACzyggDH43s0aOpCEPWZuOZereKXqkpVlEd2XOryYDC09cRkL"

def print_header(title):
    print("\n" + "=" * 70)
    print(f"  {title}")
    print("=" * 70)

def call_api(endpoint, headers=None):
    url = f"{BASE_URL}{endpoint}"
    req = urllib.request.Request(url, headers=headers or {})
    try:
        with urllib.request.urlopen(req) as response:
            status = response.status
            body = json.loads(response.read().decode("utf-8"))
            return status, body
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        try:
            body = json.loads(body)
        except Exception:
            pass
        return e.code, body
    except Exception as e:
        return 500, str(e)

# ------------------------------------------------------------------------------
# 1. Tình huống 1: Ứng dụng bên ngoài thử gọi KHÔNG có Token
# ------------------------------------------------------------------------------
print_header("TÌNH HUỐNG 1: Ứng dụng bên thứ 3 gọi API /api/users/me KHÔNG gửi Token")
print(f"Request: GET {BASE_URL}/api/users/me (Không có Header Authorization)")
status, body = call_api("/api/users/me")
print(f"--> Mã HTTP nhận được: {status}")
if status == 403:
    print("--> BẢO MẬT THÀNH CÔNG: Spring Security đã chặn đứng truy cập trái phép!")
else:
    print(f"--> Phản hồi: {body}")

# ------------------------------------------------------------------------------
# 2. Tình huống 2: Ứng dụng bên ngoài gửi Bearer Token hợp lệ của sinh viên TDTU
# ------------------------------------------------------------------------------
print_header("TÌNH HUỐNG 2: Ứng dụng bên thứ 3 gửi kèm Token JWT hợp lệ")
print(f"Request: GET {BASE_URL}/api/users/me")
print(f"Header : Authorization: Bearer {TOKEN[:35]}...")
status, body = call_api("/api/users/me", headers={"Authorization": f"Bearer {TOKEN}"})
print(f"--> Mã HTTP nhận được: {status}")
if status == 200:
    print("--> XÁC THỰC THÀNH CÔNG: Máy chủ đã giải mã thẻ JWT và cấp dữ liệu:")
    print(json.dumps(body, indent=2, ensure_ascii=False))

# ------------------------------------------------------------------------------
# 3. Tình huống 3: Ứng dụng bên ngoài lấy cấu hình Google SSO
# ------------------------------------------------------------------------------
print_header("TÌNH HUỐNG 3: Ứng dụng bên thứ 3 lấy URL đăng nhập Google SSO")
status, body = call_api("/api/auth/google/url")
print(f"--> Mã HTTP nhận được: {status}")
print(f"--> Dữ liệu trả về: {json.dumps(body, indent=2, ensure_ascii=False)}")

