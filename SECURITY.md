# Security Policy

## ⚠️ Important Security Notice

**This is a proof-of-concept/demo project.**

This repository contains demonstration code intended for educational and portfolio purposes. Security features have been intentionally simplified to focus on core functionality and architecture patterns.

## Security Features Status

### Current Implementation

- **Password Storage**: ✅ **IMPLEMENTED** - Passwords are hashed using BCrypt before storage
- **Password Hashing**: ✅ **IMPLEMENTED** - BCryptPasswordEncoder with strength 10 (configurable)
- **Authentication**: Basic token-based authentication using UUID tokens
- **Authorization**: All endpoints are publicly accessible
- **CSRF Protection**: Disabled for development/demo purposes
- **Password Migration**: ✅ **IMPLEMENTED** - Automatic migration of existing plaintext passwords on startup

### Production Requirements

**⚠️ DO NOT USE THIS CODE IN PRODUCTION WITHOUT IMPLEMENTING PROPER SECURITY MEASURES.**

A production-ready implementation should include:

- **Password Hashing**: ✅ **IMPLEMENTED** - BCrypt password hashing with configurable strength (default: 10)
  - Passwords are automatically hashed using BCryptPasswordEncoder before storage
  - Password comparison uses secure `matches()` method instead of plaintext comparison
  - Existing plaintext passwords are automatically migrated to BCrypt on application startup
  - Configuration: `password.hashing.bcrypt-strength` in `application.yml` (default: 10)
- **JWT Authentication**: Implement proper JSON Web Tokens with expiration and signing
- **Role-Based Access Control (RBAC)**: Implement proper authorization mechanisms
- **CSRF Protection**: Enable CSRF tokens for state-changing operations
- **Input Validation**: Comprehensive validation and sanitization
- **Rate Limiting**: Protect against brute-force attacks
- **HTTPS Only**: Enforce secure connections
- **Security Headers**: Implement proper security headers (CSP, HSTS, etc.)
- **Secret Management**: Use secure secret management systems
- **Audit Logging**: Log security-relevant events
- **Dependency Scanning**: Regularly scan for vulnerable dependencies

## Password Hashing Implementation (SCRUM-20)

### Implementation Details

- **Algorithm**: BCrypt (via Spring Security's BCryptPasswordEncoder)
- **Strength**: Configurable via `password.hashing.bcrypt-strength` property (default: 10)
- **Hash Format**: BCrypt hashes start with `$2a$`, `$2b$`, or `$2y$` followed by strength parameter
- **Hash Length**: 60 characters

### How It Works

1. **User Registration**: When a user is created, the plaintext password is hashed using BCrypt before storing in the database
2. **User Authentication**: During login, the provided password is compared with the stored hash using `passwordEncoder.matches()`
3. **Password Migration**: On application startup, existing plaintext passwords are automatically migrated to BCrypt hashes

### Configuration

Password hashing strength can be configured in `application.yml`:

```yaml
password:
  hashing:
    bcrypt-strength: 10  # Default: 10 (recommended), range: 10-12
```

### Security Benefits

- **Protection Against Database Breaches**: Even if the database is compromised, passwords cannot be easily recovered
- **Salt Uniqueness**: Each password gets a unique salt, preventing rainbow table attacks
- **Computational Cost**: BCrypt's strength parameter makes brute-force attacks computationally expensive
- **Industry Standard**: BCrypt is widely recognized as a secure password hashing algorithm

### Migration

The `PasswordMigrationService` automatically migrates existing plaintext passwords to BCrypt on application startup:
- Only processes passwords that are not already hashed
- Idempotent - safe to run multiple times
- Logs migration progress and statistics

## Test Credentials

Test passwords used in this project (e.g., `SecurePassword123`) are intentionally weak and are **ONLY** for automated testing purposes. These should never be used in any production or real-world scenario. All test passwords are properly hashed using BCrypt before storage.

## Reporting Security Issues

If you discover a security vulnerability in this codebase, please note that this is a demonstration project. However, if you'd like to report issues for educational purposes, please open an issue with the `security` label.

## Disclaimer

This code is provided "as-is" for demonstration purposes only. The authors assume no responsibility for any security issues that may arise from using this code in production environments without proper security implementations.
