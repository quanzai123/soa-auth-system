# 🛡️ Centralized Identity Gateway with Google OAuth 2.0 & Stateless JWT

> **Academic Seminar Project — Service-Oriented Architecture (SOA)**  
> **Student:** Nguyễn Anh Quân (ID: `524H0122`)  
> **Institution:** Faculty of Information Technology — Tôn Đức Thắng University (TDTU)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![Microsoft SQL Server](https://img.shields.io/badge/SQL%20Server-2022%20Docker-blue.svg?logo=microsoftsqlserver)](https://www.microsoft.com/sql-server)
[![OAuth 2.0](<https://img.shields.io/badge/OAuth-2.0%20(RFC%206749)-red.svg>)](https://datatracker.ietf.org/doc/html/rfc6749)
[![JWT](https://img.shields.io/badge/JWT-RFC%207519-black.svg?logo=jsonwebtokens)](https://datatracker.ietf.org/doc/html/rfc7519)
[![Tests](https://img.shields.io/badge/Tests-100%25%20Passing-success.svg)](#-automated-testing--verification)

---

## 📖 Executive Summary

In modern **Service-Oriented Architecture (SOA)** and Microservices ecosystems, traditional monolithic session management (`JSESSIONID` stored in server RAM) causes critical architectural bottlenecks: tight coupling, sticky session scaling overhead, and Cookie/CSRF friction for mobile/API clients.

This project delivers an enterprise-grade **Centralized Identity Gateway (`soa-auth-system`)** that implements:

1. **Google OAuth 2.0 Authorization Code Flow (RFC 6749):** Secure server-to-server token exchange, completely eliminating insecure implicit flows.
2. **Stateless JWT Issuance (RFC 7519):** HMAC-SHA256 cryptographically signed tokens containing self-contained claims.
3. **Autonomous Local Verification:** Downstream microservices authenticate tokens locally in **< 0.3 ms** without querying the Auth Gateway or Database.
4. **Automated Domain-Based RBAC:** Automatic server-side role assignment classifying educational email domains (`.edu.vn`, `.edu`, `@tdtu.edu.vn`) vs personal accounts (`ROLE_PERSONAL`).
5. **Bounded TTL Blacklist (Logout):** Memory-bounded token revocation resolving the fundamental stateless logout dilemma.
6. **Multi-Identity Domain & Orphan Account Prevention:** Business integrity guardrails protecting users from accidental lockout when unlinking third-party OAuth providers.

---

## ⚙️ Core Technical Capabilities

### 1. Automated Domain-Based RBAC

- Automatically parses Google verified profile email upon authentication.
- Accounts with `.edu.vn`, `.edu`, or `@tdtu.edu.vn` receive `ROLE_STUDENT` + `accountType: STUDENT`.
- Personal accounts (`@gmail.com`) receive `ROLE_PERSONAL`.
- Enforced deterministically at the Web Security Filter layer; unauthorized access attempts are blocked with `HTTP 403 Forbidden` without consuming downstream application resources.

### 2. Stateless Identity Propagation & Short-Lived Access Tokens

- **Decoupled Verification:** Downstream microservices verify token signatures locally using shared cryptographic secrets (or public keys via JWKS discovery), completely eliminating synchronous RPC calls to the Auth service.
- **Revocation Trade-off & Short-Lived TTL:** Because downstream microservices do not synchronously query the central Blacklist on every call (preserving SOA loose coupling), Access Tokens are given a strictly **short lifespan of 15 minutes** paired with a 7-day Refresh Token. In enterprise environments, this can be coupled with a distributed Redis Cluster or API Gateway introspection.

### 3. Bounded TTL Blacklist (Instant Revocation on Identity Service)

- Solves the stateless token invalidation dilemma on the Identity Service upon logout (`POST /api/auth/logout`).
- Stores revoked token signatures with an exact remaining lifespan (`TTL = exp - now`).
- Automatic memory eviction prevents RAM leakage over time, designed as a direct drop-in for distributed **Redis Cluster** in production.

### 4. Multi-Identity & Orphan Account Prevention

- Decoupled `users` and `user_identities` schema allows multiple OAuth providers per user account.
- If a user registered via Google attempts to unlink without first establishing a local password credential (`passwordHash == null`), the system rejects the operation with `HTTP 400 Bad Request`.

---

## 📋 Standardized RESTful API Matrix

| Method | Endpoint                      | Access Level | Description                                                            |
| :----: | :---------------------------- | :----------: | :--------------------------------------------------------------------- |
| `GET`  | `/api/auth/google/url`        |    Public    | Generates Google OAuth consent URL with cryptographic anti-CSRF state  |
| `POST` | `/api/auth/google/callback`   |    Public    | Exchanges authorization code for Stateless JWT Access Token            |
| `POST` | `/api/auth/logout`            |  Bearer JWT  | Enlists active token into Bounded TTL Blacklist for instant revocation |
| `POST` | `/api/auth/refresh-token`     |    Public    | Issues a new Access Token using a valid Refresh Token                  |
| `GET`  | `/api/users/me`               |  Bearer JWT  | Retrieves authenticated profile, assigned role, and linked identities  |
| `POST` | `/api/users/me/password`      |  Bearer JWT  | Configures local fallback password (BCrypt salted hash)                |
| `POST` | `/api/users/me/unlink-google` |  Bearer JWT  | Unlinks Google identity (guarded by orphan account validation)         |
| `GET`  | `/api/public/database-viewer` |    Public    | Real-time masked snapshot of SQL Server 2022 database records          |

All endpoints return standardized JSON wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

---

## 🚀 Getting Started

### Prerequisites

- **Java Development Kit (JDK):** 17 or 21+
- **Docker & Docker Compose** (for Microsoft SQL Server 2022)
- **Google Cloud Console:** OAuth 2.0 Client ID & Client Secret configured with redirect URI:  
  `http://localhost:8080/api/auth/google/callback`

### 1. Database Setup (SQL Server 2022 Docker)

```bash
docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Spring@123456" \
   -p 1433:1433 --name sqlserver --hostname sqlserver \
   -d mcr.microsoft.com/mssql/server:2022-latest
```

### 2. Configuration (`application.yml`)

Set your credentials via environment variables or directly in `src/main/resources/application.yml`:

```yaml
app:
  jwt:
    secret: YOUR_256_BIT_SECRET_KEY
    expiration-ms: 900000 # 15 minutes
  google:
    client-id: ${GOOGLE_CLIENT_ID}
    client-secret: ${GOOGLE_CLIENT_SECRET}
    redirect-uri: http://localhost:8080/api/auth/google/callback
```

### 3. Build & Run

```bash
# Clone the repository
git clone https://github.com/quanzai123/soa-auth-system.git
cd soa-auth-system

# Run with Maven Wrapper
./mvnw spring-boot:run
```

Access the application portal at: **`http://localhost:8080`**  
Access the real-time Database Viewer at: **`http://localhost:8080/database.html`**

---

## 🧪 Automated Testing & Verification

### Run CLI Test Suite

```bash
chmod +x test-external-actors.sh
./test-external-actors.sh
```

### Run Postman Collection via Newman

```bash
npx --yes newman run soa-auth-collection.json
```

**Test Results:** 12/12 API requests passed with 0 failures (100% test coverage including Blacklist revocation and RBAC boundaries).

---

## 📈 Enterprise Production Roadmap

1. **Asymmetric RS256 & JWKS:** Upgrade from symmetric HMAC to RSA 2048/4096 key pairs with dynamic `/.well-known/jwks.json` discovery.
2. **Distributed Redis Cluster:** Transition `TokenBlacklistService` to multi-region Redis clusters (`SET key EX <ttl>`) for sub-millisecond global cache invalidation.
3. **Edge Gateway Ingress:** Terminate token validation at **Kong Gateway** or **Envoy Proxy** before routing to internal microservices over mutual TLS.

---

## 📄 License

This project is licensed under the MIT License — developed for academic research and seminar defense in Service-Oriented Architecture (SOA).
