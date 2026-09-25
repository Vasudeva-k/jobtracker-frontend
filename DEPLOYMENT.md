# JobTracker — Production Deployment Guide

This document provides complete instructions for deploying the **JobTracker** application to production environments.

---

## 1. Architecture Overview

JobTracker is a modern full-stack application composed of:
- **Backend**: Spring Boot 3 / 4 (Java 21), Spring Data JPA, Spring Security, JWT stateless authentication.
- **Frontend**: React 19 SPA built with Vite, React Router, and Lucide icons.
- **Database**: MySQL 8.0+ with indexed relational tables and foreign keys.
- **AI Engine**: Google Gemini API integration for resume parsing, interview generation, ATS scoring, and career roadmaps.
- **Storage**: Local filesystem storage (configurable path) with path-traversal sanitization.

---

## 2. Prerequisites

| Component | Required Version | Verification Command |
| :--- | :--- | :--- |
| **Java JDK** | 21+ | `java -version` |
| **Node.js** | 20.x or 22.x LTS | `node -v` |
| **npm** | 10.x+ | `npm -v` |
| **MySQL Server** | 8.0+ | `mysql --version` |

---

## 3. Environment Variables Reference

### Backend (`jobtracker`)

| Variable Name | Required | Default / Example | Description |
| :--- | :---: | :--- | :--- |
| `PORT` | No | `8081` | HTTP port for backend service |
| `SPRING_DATASOURCE_URL` | **Yes** | `jdbc:mysql://localhost:3306/jobtracker?useSSL=true&requireSSL=true` | JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | **Yes** | `jobtracker_user` | MySQL database username |
| `SPRING_DATASOURCE_PASSWORD` | **Yes** | `[SECURE_PASSWORD]` | MySQL database password |
| `JWT_SECRET` | **Yes** | `[256-bit Base64 String]` | Secret key for signing JWT tokens |
| `JWT_EXPIRATION` | No | `86400000` (24h in ms) | JWT validity duration |
| `CORS_ALLOWED_ORIGINS` | **Yes** | `https://yourdomain.com` | Comma-separated list of allowed frontend origins |
| `FILE_UPLOAD_DIR` | No | `uploads/resumes` | Directory path for stored resumes |
| `GEMINI_API_KEY` | **Yes** | `AIzaSy...` | Google Gemini API Key |
| `GEMINI_API_URL` | No | `https://generativelanguage.googleapis.com/...` | Gemini endpoint URL |
| `SPRING_MAIL_HOST` | No | `smtp.gmail.com` | SMTP host |
| `SPRING_MAIL_PORT` | No | `587` | SMTP port |
| `SPRING_MAIL_USERNAME` | No | `your-email@gmail.com` | SMTP username |
| `SPRING_MAIL_PASSWORD` | No | `[APP_PASSWORD]` | SMTP app password |
| `SPRING_MAIL_FROM` | No | `noreply@yourdomain.com` | Email sender address |
| `HIBERNATE_DDL_AUTO` | No | `validate` or `update` | Hibernate schema mode |

### Frontend (`frontend`)

| Variable Name | Required | Default / Example | Description |
| :--- | :---: | :--- | :--- |
| `VITE_API_BASE_URL` | **Yes** | `https://api.yourdomain.com` | Base URL of the backend REST API |

---

## 4. Database Setup

1. Connect to MySQL server:
   ```bash
   mysql -u root -p
   ```
2. Create database and dedicated user:
   ```sql
   CREATE DATABASE jobtracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'jobtracker_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
   GRANT ALL PRIVILEGES ON jobtracker.* TO 'jobtracker_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. For initial deployment, start the backend with `HIBERNATE_DDL_AUTO=update` to generate tables and indexes, then transition to `HIBERNATE_DDL_AUTO=validate`.

---

## 5. Backend Production Build & Startup

1. Navigate to backend directory:
   ```bash
   cd jobtracker
   ```
2. Execute production test suite & package jar:
   ```bash
   # Linux / macOS
   ./mvnw clean package -DskipTests=false

   # Windows
   .\mvnw.cmd clean package -DskipTests=false
   ```
3. Run the compiled JAR with production profile:
   ```bash
   java -jar target/jobtracker-0.0.1-SNAPSHOT.jar --spring.config.location=classpath:/application.properties
   ```
   Or pass environment variables:
   ```bash
   export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/jobtracker?useSSL=true&requireSSL=true"
   export SPRING_DATASOURCE_USERNAME="jobtracker_user"
   export SPRING_DATASOURCE_PASSWORD="StrongPassword123!"
   export JWT_SECRET="YzNkZjY3OWJmYzMwNTNkNjQ3YjY5MzEzMjNmNjQ5NjJjYmM5MjM0NjViY2Q3N2QxYzQ0YzQ4N2MwNmQ0M2M1MQ=="
   export CORS_ALLOWED_ORIGINS="https://jobtracker.yourdomain.com"
   export GEMINI_API_KEY="your-gemini-key"
   java -jar target/jobtracker-0.0.1-SNAPSHOT.jar
   ```

---

## 6. Frontend Production Build & Hosting

1. Navigate to frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm ci
   ```
3. Configure environment variable:
   Create `.env.production`:
   ```env
   VITE_API_BASE_URL=https://api.yourdomain.com
   ```
4. Build optimized production bundle:
   ```bash
   npm run build
   ```
5. Deploy `frontend/dist/` to your static host (Nginx, Caddy, Vercel, AWS S3 + CloudFront).

### Example Nginx Configuration
```nginx
server {
    listen 80;
    server_name jobtracker.yourdomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name jobtracker.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/jobtracker.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/jobtracker.yourdomain.com/privkey.pem;

    # Frontend Single Page App
    location / {
        root /var/www/jobtracker/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Reverse proxy backend API
    location /api/ {
        proxy_pass http://localhost:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 7. Production Security Checklist

- [x] **No Hardcoded Secrets**: Secrets are sourced from environment variables.
- [x] **Strict CORS**: Only trusted domains configured in `CORS_ALLOWED_ORIGINS` are accepted.
- [x] **JWT Expiration & Verification**: All incoming requests are authenticated via `JwtAuthenticationFilter`.
- [x] **Path Traversal Sanitization**: Uploaded filenames are sanitized against `../`, `..\`, null bytes, and absolute paths in `ResumeStorageServiceImpl`.
- [x] **Database Isolation & IDOR Protection**: All user resource queries are scoped by authenticated user ID.
- [x] **Safe Exception Handling**: Stack traces, class names, and SQL errors are suppressed in `GlobalExceptionHandler`.
- [x] **AI Engine Fallbacks**: Outages or malformed responses from Gemini are gracefully captured without crashing requests.

---

## 8. Verification Commands

Run the full verification suite anytime before releasing:

```powershell
# Backend Verification (105 tests)
cd jobtracker
.\mvnw.cmd clean test

# Frontend Linting (0 errors)
cd frontend
npm.cmd run lint

# Frontend Production Build (Zero errors)
cd frontend
npm.cmd run build
```
