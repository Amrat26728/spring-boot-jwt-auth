# JwtAuth

JwtAuth is a Spring Boot REST API project that implements JWT-based authentication with access tokens and refresh tokens. It provides user registration, login, token refresh, password hashing, stateless security, and persisted refresh-token sessions.

## Features

- User registration with full name, username, password, and default `USER` role
- BCrypt password hashing through Spring Security
- JWT access-token generation for authenticated users
- Refresh-token generation, hashing, persistence, expiration, and rotation
- Refresh-token reuse detection with session revocation
- Logout support with access-token revocation
- Revoked access-token tracking by JWT `jti` until the token expires
- Stateless Spring Security configuration
- Bearer-token authentication filter for protected routes
- JPA entities for users, refresh tokens, and revoked access tokens
- Repository layer using Spring Data JPA
- DTO-based request and response models
- ModelMapper support for entity-to-DTO conversion

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- JJWT
- Lombok
- ModelMapper
- Maven

## Project Structure

```text
src/main/java/com/amrat/JwtAuth
+-- config        # Beans for password encoding, model mapping, and authentication manager
+-- controller    # Authentication REST endpoints
+-- dto           # Request and response DTOs
+-- entity        # User and refresh-token JPA entities
+-- repository    # Spring Data JPA repositories
+-- security      # JWT filter and security configuration
+-- service       # Authentication and user services
+-- util          # JWT and token hashing utilities
```

## API Endpoints

Base path:

```text
/api/v1/auth
```

### Register

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "fullName": "John Doe",
  "username": "john",
  "password": "password123"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "username": "john",
  "password": "password123"
}
```

Response:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "raw-refresh-token"
}
```

### Refresh Token

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "raw-refresh-token"
}
```

Response:

```json
{
  "accessToken": "new-jwt-access-token",
  "refreshToken": "new-raw-refresh-token"
}
```

### Logout

```http
POST /api/v1/auth/logout
Authorization: Bearer your-jwt-access-token
```

Response:

```text
Logged out successfully.
```

Logout performs two actions:

1. Extracts the current access token from the `Authorization` header.
2. Reads the token `jti` and expiry time.
3. Saves the `jti` in `revoked_access_tokens` until the access token naturally expires.
4. Revokes all refresh tokens for the current authenticated user.
5. Clears the current Spring Security context.

After logout, the same access token cannot be used again. `JwtAuthFilter` checks each incoming Bearer token against `RevokedAccessTokenRepository`; if the token `jti` is found, the request is rejected.

## Authentication

Protected endpoints should send the JWT access token in the `Authorization` header:

```http
Authorization: Bearer your-jwt-access-token
```

The security configuration allows `register`, `login`, and `refresh` without authentication. The `logout` route and all other protected routes require a valid Bearer access token.

Access tokens include a JWT ID (`jti`) claim. This ID is used for logout-based revocation without storing the full raw access token.

## Configuration

The JWT secret key is configured in:

```text
src/main/resources/application.properties
```

Example:

```properties
jwt.secretKey=your-long-secure-secret-key
```

Add your database connection settings in `application.properties` before running the application with JPA.

## Run Locally

Use the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

