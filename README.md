# JobTracker — AI-Powered Job Application Tracking & Career Platform

JobTracker is a full-stack, enterprise-ready web application designed to help job seekers organize, track, and accelerate their job search with intelligent AI tools powered by Google Gemini.

---

## Key Features

### 1. Application Tracker & Kanban Board
- Track job applications with statuses: `APPLIED`, `INTERVIEW`, `OFFERED`, `REJECTED`, `SAVED`.
- Switch seamlessly between **Interactive Table View** and **Drag-and-Drop / Inline Kanban Board**.
- Filter by status, employment type, location, and sort by salary, date, or company name.
- Track upcoming interviews with dedicated date/time reminders.

### 2. AI Career Toolkit (Powered by Google Gemini)
- **Cover Letter Generator**: Generate tailored, ATS-friendly cover letters from job descriptions.
- **Interview Question Generator**: Generate role-specific technical and behavioral questions.
- **Career Roadmap Planner**: Structured, step-by-step career path roadmaps.
- **Skill Gap Analyzer**: Identify missing technologies and generate concrete learning timelines.
- **Salary Predictor**: Predict compensation ranges based on experience and tech stack.
- **Resume vs Job Matcher**: Compare resumes against job postings for keyword relevance.
- **ATS Score Predictor**: Detailed resume scoring, keyword coverage analysis, and formatting suggestions.
- **Mock Interview Simulator**: Practice questions and receive immediate scored feedback.
- **Job Market Trends**: Real-time insights into in-demand skills and salary trends.
- **Company Interview Experiences**: Curated technical questions, HR questions, and interview tips.
- **AI Career Chat**: Interactive conversational career advisor.

### 3. Resume Hub
- PDF Resume upload with automatic text extraction (Apache PDFBox).
- Automated AI ATS analysis and keyword breakdown.
- Secure, isolated file storage with path-traversal sanitization.
- Clean PDF download and resume metadata management.

### 4. Admin Management & Analytics
- Complete admin dashboard with monthly application trends, status distributions, and role metrics.
- User management: Search, filter, block, unblock, and deactivate user accounts.
- Application oversight: Global search, status filtering, and audit management.

---

## Technology Stack

- **Backend**:
  - Java 21 LTS
  - Spring Boot 4 / Spring Framework 6
  - Spring Data JPA / Hibernate 7
  - Spring Security (Stateless JWT Authentication)
  - Apache PDFBox (PDF Text Extraction)
  - MySQL 8.0 Connector
  - JUnit 5 & Mockito
- **Frontend**:
  - React 19
  - Vite 8
  - React Router 7
  - Recharts (Interactive Analytics Visualizations)
  - Lucide React (Icons)
  - Vanilla CSS Modern Design System (Glassmorphism, Dark/Light palettes)
- **AI Integration**:
  - Google Gemini API (`gemini-3.5-flash`)
- **Database**:
  - MySQL 8.0+

---

## Project Structure

```
jobtracker/
├── frontend/                  # React Vite Single Page Application
│   ├── src/
│   │   ├── components/        # Reusable UI components
│   │   ├── pages/             # Auth, User, and Admin views
│   │   ├── services/          # API service client (api.js)
│   │   └── App.jsx            # Routing and Protected Routes
│   ├── package.json
│   └── .env.example
├── jobtracker/                # Spring Boot Backend Service
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/vasudev/jobtracker/
│   │   │   │   ├── ai/        # AI engines and fallback analyzers
│   │   │   │   ├── controller/# REST API Controllers
│   │   │   │   ├── dto/       # Request and Response DTOs
│   │   │   │   ├── entity/    # JPA Entities (User, JobApplication, Resume, Profile)
│   │   │   │   ├── exception/ # Global Exception Handler
│   │   │   │   ├── repository/# Spring Data JPA Repositories
│   │   │   │   ├── security/  # JWT Filters and Security Configuration
│   │   │   │   ├── service/   # Business Logic Services
│   │   │   │   └── util/      # PDF and String utilities
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── application-prod.properties.example
│   │   └── test/              # Comprehensive JUnit 5 Test Suite (105+ tests)
│   └── pom.xml
├── DEPLOYMENT.md              # Complete Production Deployment Guide
└── README.md
```

---

## Getting Started

### Prerequisites
- **Java JDK 21+**
- **Node.js 20+** & **npm 10+**
- **MySQL Server 8.0+**

### 1. Database Setup
```sql
CREATE DATABASE jobtracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'jobtracker_user'@'localhost' IDENTIFIED BY 'YourSecurePassword';
GRANT ALL PRIVILEGES ON jobtracker.* TO 'jobtracker_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Backend Setup & Startup
```powershell
cd jobtracker

# Configure environment variables (or update application.properties)
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/jobtracker?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME="jobtracker_user"
$env:SPRING_DATASOURCE_PASSWORD="YourSecurePassword"
$env:JWT_SECRET="YzNkZjY3OWJmYzMwNTNkNjQ3YjY5MzEzMjNmNjQ5NjJjYmM5MjM0NjViY2Q3N2QxYzQ0YzQ4N2MwNmQ0M2M1MQ=="
$env:GEMINI_API_KEY="your-gemini-api-key"

# Run tests and start backend
.\mvnw.cmd spring-boot:run
```
The backend starts on `http://localhost:8081`.

### 3. Frontend Setup & Startup
```powershell
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```
The frontend starts on `http://localhost:5173`.

---

## Testing & Quality Assurance

Run the comprehensive automated test suite:

```powershell
# Backend Test Suite (105+ tests covering Security, AI fallbacks, Storage, IDOR, Services)
cd jobtracker
.\mvnw.cmd clean test

# Frontend ESLint (0 errors, 0 warnings)
cd frontend
npm.cmd run lint

# Frontend Production Build Verification
cd frontend
npm.cmd run build
```

---

## Production Deployment & Packaging

### Backend Package
```powershell
cd jobtracker
.\mvnw.cmd clean package -DskipTests
# Generates target/jobtracker-0.0.1-SNAPSHOT.jar
```

### Frontend Build
```powershell
cd frontend
npm run build
# Generates optimized static assets in dist/
```

For complete production deployment, Nginx reverse proxy configuration, and container instructions, see [`DEPLOYMENT.md`](file:///c:/Users/vasud/Downloads/jobtracker/DEPLOYMENT.md).

---

## Security Architecture

- **Stateless JWT**: Secure token verification via `JwtAuthenticationFilter` with role-based access control (`ROLE_USER`, `ROLE_ADMIN`).
- **IDOR Protection**: Strict ownership validation on all job applications, resumes, and user profiles.
- **Storage Isolation**: Path-traversal sanitization (`../`, `..\`, null bytes, absolute paths) in `ResumeStorageServiceImpl`.
- **Clean Exception Handling**: Generic and sanitized client responses in `GlobalExceptionHandler` with zero leakage of stack traces, class names, or SQL internals.
- **Resilient AI Engines**: Guaranteed zero unhandled `NullPointerException`s, automatic markdown fence stripping, and fallback structures for AI rate limits or outages.

---

## License

This project is licensed under the MIT License.
