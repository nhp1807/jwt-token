# JWT Authentication System

Hệ thống xác thực JWT (JSON Web Token) với Access Token và Refresh Token được xây dựng bằng Spring Boot.

## 🚀 Tính năng

### ✅ Xác thực và Phân quyền
- **JWT Authentication** với Access Token (15 phút) và Refresh Token (7 ngày)
- **Role-based Authorization** (USER, ADMIN)
- **Password Encryption** với BCrypt
- **Stateless Session Management**
- **Google OAuth2 Login**: Đăng nhập bằng tài khoản Google
- **Facebook OAuth2 Login**: Đăng nhập bằng tài khoản Facebook

### ✅ Bảo mật nâng cao
- **Refresh Token Rotation** khi đăng nhập lại (bao gồm cả đăng nhập Google và Facebook)
- **Token Storage** trong database để có thể revoke
- **Automatic Token Cleanup** (scheduled task)
- **Comprehensive Error Handling** với thông báo tiếng Việt

### ✅ Token Management
- **Access Token**: Hết hạn sau 15 phút
- **Refresh Token**: Hết hạn sau 7 ngày
- **Token Storage**: Refresh token được lưu trong database
- **Token Rotation**: Refresh token được thay đổi khi đăng nhập lại (bao gồm cả đăng nhập Google). Khi đăng nhập Google, hệ thống sẽ xóa refresh token cũ của user (nếu có) và tạo refresh token mới, đảm bảo mỗi user chỉ có 1 refresh token hợp lệ.

### ✅ API Endpoints
- **Đăng ký tài khoản** (`POST /api/v1/auth/register`)
- **Đăng nhập** (`POST /api/v1/auth/authenticate`)
- **Đăng nhập bằng Google** (`POST /api/v1/auth/google`)
- **Đăng nhập bằng Facebook** (`POST /api/v1/auth/facebook`)
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

#### 3. Đăng nhập bằng Google
```http
POST /api/v1/auth/google
Content-Type: application/json

{
  "idToken": "<Google_ID_Token>"
}
```
**Chú ý:** Khi đăng nhập bằng Google, hệ thống sẽ xóa refresh token cũ của user (nếu có) và tạo refresh token mới.

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 4. Đăng nhập bằng Facebook
```http
POST /api/v1/auth/facebook
Content-Type: application/json

{
  "accessToken": "<Facebook_Access_Token>"
}
```
**Chú ý:** Khi đăng nhập bằng Facebook, hệ thống sẽ xóa refresh token cũ của user (nếu có) và tạo refresh token mới.

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 5. Refresh Token
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

#### 6. Logout
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
- **Token Rotation**: Refresh token được thay đổi khi đăng nhập lại (bao gồm cả đăng nhập Google và Facebook). Khi đăng nhập bằng OAuth2 (Google/Facebook), hệ thống sẽ xóa refresh token cũ của user (nếu có) và tạo refresh token mới, đảm bảo mỗi user chỉ có 1 refresh token hợp lệ.

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
│   ├── GoogleAuthService.java
│   ├── FacebookAuthService.java
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
│   │   ├── RefreshTokenRequest.java
│   │   ├── GoogleAuthRequest.java
│   │   └── FacebookAuthRequest.java
│   └── response/             # Response DTOs
│       ├── AuthenticationResponse.java
│       ├── GoogleUserInfo.java
│       ├── FacebookUserInfo.java
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
- **`users`**: Thông tin người dùng
- **`refresh_tokens`**: Refresh token storage

## 🔄 Luồng hoạt động

### 1. Đăng ký/Đăng nhập/Đăng nhập OAuth2 (Google/Facebook)
1. User gửi credentials, Google ID token, hoặc Facebook access token
2. Server xác thực và tạo access token + refresh token
3. Nếu là đăng nhập lại (bao gồm OAuth2), refresh token cũ sẽ bị xóa khỏi database, chỉ giữ lại refresh token mới nhất
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

#### 3. Đăng nhập bằng Google
```bash
curl -X POST http://localhost:8080/api/v1/auth/google \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "<Google_ID_Token>"
  }'
```

#### 4. Đăng nhập bằng Facebook
```bash
curl -X POST http://localhost:8080/api/v1/auth/facebook \
  -H "Content-Type: application/json" \
  -d '{
    "accessToken": "<Facebook_Access_Token>"
  }'
```

#### 5. Truy cập API được bảo vệ
```bash
curl -X GET http://localhost:8080/api/v1/demo-controller \
  -H "Authorization: Bearer <access_token>"
```

### Test với HTML Pages

Dự án bao gồm các trang HTML để test đăng nhập:

#### 1. Test đăng nhập Google
Mở file `google-signin-test.html` trong trình duyệt để test đăng nhập Google.

#### 2. Test đăng nhập Facebook
Mở file `facebook-signin-test.html` trong trình duyệt để test đăng nhập Facebook.

## 📝 Cấu hình

### 1. File cấu hình `application.properties`
Tạo file `src/main/resources/application.properties` với nội dung ví dụ:

```properties
# Thông tin database
spring.datasource.url=jdbc:mysql://localhost:3306/jwt_security
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.database=mysql
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# JWT Secret (Base64 encoded)
jwt.secret=YOUR_BASE64_SECRET_KEY

# Google OAuth2 Configuration
google.oauth2.client-id=YOUR_GOOGLE_CLIENT_ID
google.oauth2.client-secret=YOUR_GOOGLE_CLIENT_SECRET
google.oauth2.redirect-uri=http://localhost:8080/api/v1/auth/google/callback

# Facebook OAuth2 Configuration
facebook.oauth2.app-id=YOUR_FACEBOOK_APP_ID
facebook.oauth2.app-secret=YOUR_FACEBOOK_APP_SECRET
facebook.oauth2.redirect-uri=http://localhost:8080/api/v1/auth/facebook/callback

# Token expiration time
token.access-token-expiration=90000 # 15 phút
token.refresh-token-expiration=60480000 # 7 ngày
```

**Lưu ý bảo mật:**
- KHÔNG commit `google.oauth2.client-secret`, `facebook.oauth2.app-secret` hoặc thông tin nhạy cảm lên git.
- Nên thêm `src/main/resources/application.properties` vào `.gitignore`.
- Khi deploy, sử dụng biến môi trường hoặc file cấu hình riêng cho secret.

### 2. Cấu hình biến môi trường (tùy chọn)
Bạn có thể truyền các giá trị nhạy cảm qua biến môi trường khi chạy ứng dụng:
```sh
mvn spring-boot:run -Dspring-boot.run.arguments="--google.oauth2.client-secret=YOUR_SECRET --facebook.oauth2.app-secret=YOUR_SECRET"
```

### 3. Cấu hình Facebook App
Để sử dụng đăng nhập Facebook, bạn cần:
1. Tạo Facebook App tại [Facebook Developers](https://developers.facebook.com/)
2. Thêm Facebook Login product vào app
3. Cấu hình Valid OAuth Redirect URIs trong Facebook Login Settings
4. Thêm App Domains (localhost, 127.0.0.1) trong Basic Settings
5. Cập nhật App ID và App Secret vào `application.properties`

### 4. Tạo file cấu hình local (không commit)
Tạo file `application-local.properties` (không commit lên git) để lưu thông tin nhạy cảm khi phát triển local.