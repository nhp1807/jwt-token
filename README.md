# JWT Authentication System

Hệ thống xác thực JWT (JSON Web Token) với Access Token và Refresh Token được xây dựng bằng Spring Boot.

## 🚀 Tính năng

### ✅ Xác thực và Phân quyền
- **JWT Authentication** với Access Token (15 phút) và Refresh Token (7 ngày)
- **Role-based Authorization** (USER, ADMIN)
- **Password Encryption** với BCrypt
- **Stateless Session Management**

### ✅ Bảo mật nâng cao
- **Refresh Token Rotation** khi đăng nhập lại
- **Token Storage** trong database để có thể revoke
- **Automatic Token Cleanup** (scheduled task)
- **Comprehensive Error Handling** với thông báo tiếng Việt

### ✅ API Endpoints
- **Đăng ký tài khoản** (`POST /api/v1/auth/register`)
- **Đăng nhập** (`POST /api/v1/auth/authenticate`)
- **Refresh Token** (`POST /api/v1/auth/refresh-token`)
- **Logout** (`POST /api/v1/auth/logout`)
- **Demo API** (`GET /api/v1/demo-controller`)

## 🛠️ Công nghệ sử dụng

- **Spring Boot 3.1.4** (Java 17)
- **Spring Security** với JWT
- **Spring Data JPA** + **MySQL**
- **JJWT Library** (JWT implementation)
- **Lombok** (code generation)
- **Maven** (dependency management)

## 📋 Yêu cầu hệ thống

- **Java 17** trở lên
- **MySQL 8.0** trở lên
- **Maven 3.6** trở lên

## ⚙️ Cài đặt và Chạy

### 1. Clone repository
```bash
git clone <repository-url>
cd jwt-token
```

### 2. Cấu hình database
Tạo database MySQL và cập nhật thông tin trong `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jwt_security
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Chạy ứng dụng
```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## 📚 API Documentation

### 🔐 Authentication Endpoints

#### 1. Đăng ký tài khoản
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 2. Đăng nhập
```http
POST /api/v1/auth/authenticate
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 3. Refresh Token
```http
POST /api/v1/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 4. Logout
```http
POST /api/v1/auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response:**
```json
"Logged out successfully"
```

### 🔒 Protected Endpoints

#### Demo API
```http
GET /api/v1/demo-controller
Authorization: Bearer <access_token>
```

**Response:**
```json
"Hello from secured endpoint"
```

## 🔐 Bảo mật

### Token Management
- **Access Token**: Hết hạn sau 15 phút
- **Refresh Token**: Hết hạn sau 7 ngày
- **Token Storage**: Refresh token được lưu trong database
- **Token Rotation**: Refresh token được thay đổi khi đăng nhập lại

### Error Handling
Hệ thống trả về thông báo lỗi chi tiết bằng tiếng Việt:

```json
{
  "timestamp": "2024-01-17T10:30:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token không hợp lệ hoặc đã hết hạn",
  "path": "/api/v1/demo-controller"
}
```

### Các loại lỗi
| HTTP Status | Tình huống | Message |
|-------------|------------|---------|
| 401 | Không có token | "Xác thực thất bại. Vui lòng đăng nhập lại" |
| 401 | Token không hợp lệ | "Token không hợp lệ hoặc đã hết hạn" |
| 401 | Email/password sai | "Email hoặc mật khẩu không đúng" |
| 403 | Không có quyền | "Bạn không có quyền truy cập vào tài nguyên này" |
| 400 | Refresh token sai | "Invalid refresh token" |

## 🏗️ Kiến trúc

### Package Structure
```
src/main/java/com/example/security/
├── controller/               # REST Controllers
│   ├── AuthenticationController.java
│   └── DemoController.java
├── service/                  # Business Logic
│   ├── AuthenticationService.java
│   ├── JwtService.java
│   └── ScheduledTasks.java
├── model/                    # Entity Models
│   ├── User.java
│   └── RefreshToken.java
├── repository/               # Data Access Layer
│   ├── UserRepository.java
│   └── RefreshTokenRepository.java
├── dto/                      # Data Transfer Objects
│   ├── request/              # Request DTOs
│   │   ├── AuthenticationRequest.java
│   │   ├── RegisterRequest.java
│   │   └── RefreshTokenRequest.java
│   └── response/             # Response DTOs
│       ├── AuthenticationResponse.java
│       └── ErrorResponse.java
├── config/                   # Configuration
│   ├── SecurityConfiguration.java
│   ├── JwtAuthenticationFilter.java
│   └── ApplicationConfig.java
├── enums/                    # Enumerations
│   └── Role.java
├── exception/                # Error handling
│   └── GlobalExceptionHandler.java
└── SecurityApplication.java  # Main Application
```

### Database Schema
- **`_user`**: Thông tin người dùng
- **`refresh_tokens`**: Refresh token storage

## 🔄 Luồng hoạt động

### 1. Đăng ký/Đăng nhập
1. User gửi credentials
2. Server xác thực và tạo access token + refresh token
3. Refresh token được lưu vào database
4. Server trả về cả 2 token

### 2. Sử dụng API
1. Client gửi access token trong header `Authorization: Bearer <token>`
2. JWT Filter kiểm tra và xác thực token
3. Nếu hợp lệ → cho phép truy cập API
4. Nếu không hợp lệ → trả về lỗi 401

### 3. Refresh Token
1. Access token hết hạn
2. Client gửi refresh token
3. Server kiểm tra refresh token trong database
4. Nếu hợp lệ → tạo access token mới
5. Trả về access token mới + refresh token cũ

### 4. Logout
1. Client gửi refresh token
2. Server xóa refresh token khỏi database
3. Token bị vô hiệu hóa

## 🧪 Testing

### Test với Postman/curl

#### 1. Đăng ký
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### 2. Đăng nhập
```bash
curl -X POST http://localhost:8080/api/v1/auth/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### 3. Truy cập API được bảo vệ
```bash
curl -X GET http://localhost:8080/api/v1/demo-controller \
  -H "Authorization: Bearer <access_token>"
```

## 📝 Cấu hình

### JWT Configuration
- **Secret Key**: Base64 encoded trong `JwtService.java`
- **Access Token Expiration**: 15 phút
- **Refresh Token Expiration**: 7 ngày
- **Algorithm**: HS256

### Database Configuration
- **Auto Create/Drop**: `spring.jpa.hibernate.ddl-auto=update`
- **Show SQL**: `spring.jpa.show-sql=true`
- **Dialect**: MySQL8Dialect

### Scheduled Tasks
- **Token Cleanup**: Chạy mỗi ngày lúc 2:00 AM
- **Purpose**: Xóa refresh token hết hạn