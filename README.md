# Fully-Compliant-Pipeline

A full-stack e-commerce proof-of-concept application demonstrating modern software development practices, CI/CD pipelines, and cloud deployment strategies.

## ⚠️ Security Notice

**This is a demonstration/proof-of-concept project.** Security features (password hashing, authentication, etc.) are intentionally simplified for educational purposes. 

**Production applications should implement proper security measures including:**
- Password hashing (BCrypt/Argon2)
- JWT-based authentication
- Role-based access control
- CSRF protection
- Input validation and sanitization
- Rate limiting
- Security headers (CSP, HSTS, etc.)
- Secure secret management

For more details, see [SECURITY.md](SECURITY.md).

## Overview

This project demonstrates:
- Full-stack development (Java Spring Boot backend, React TypeScript frontend)
- CI/CD pipelines with GitHub Actions
- Cloud deployment to Azure App Service
- Container orchestration with Kubernetes
- End-to-end testing with Selenium
- Database migrations with Liquibase
- Feature toggles and configuration management

## Project Structure

```
├── api/                    # Backend services (Spring Boot)
├── ui/                     # Frontend application (React/TypeScript)
├── selenium/               # End-to-end tests
├── k8s/                    # Kubernetes manifests
├── scripts/                # Deployment and utility scripts
├── docs/                   # Documentation
└── .github/workflows/      # CI/CD pipeline definitions
```

## Getting Started

See individual component READMEs:
- [UI README](ui/README.md)
- [Selenium Tests README](selenium/README.md)
- [Kubernetes README](k8s/README.md)

## Documentation

- [Database Setup](docs/DATABASE_SETUP.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Security Policy](SECURITY.md)

## License

This project is for demonstration purposes only.
