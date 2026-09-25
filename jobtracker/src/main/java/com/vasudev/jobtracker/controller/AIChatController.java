package com.vasudev.jobtracker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasudev.jobtracker.dto.ChatResponse;
import com.vasudev.jobtracker.exception.GeminiException;
import com.vasudev.jobtracker.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIChatController {

    private static final Logger log = LoggerFactory.getLogger(AIChatController.class);
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AIChatController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatResponse> chatJson(@RequestBody(required = false) String rawBody) {
        ChatResponse response = processChat(rawBody);
        return ResponseEntity.ok(response);
    }

    public String chat(String prompt) {
        return processChat(prompt).getResponse();
    }

    private ChatResponse processChat(String rawBody) {
        String cleanPrompt = extractPromptText(rawBody);

        if (cleanPrompt.isBlank()) {
            return new ChatResponse(
                    "Hello! I am your AI Career Advisor. How can I help you with your job search, resume, career roadmap, or interview preparation today?",
                    "FALLBACK"
            );
        }

        boolean geminiAvailable = geminiService != null && geminiService.isAvailable();
        log.info("[AI] Feature: AI Career Chat");
        log.info("[AI] Gemini available: {}", geminiAvailable);

        if (geminiAvailable) {
            try {
                log.info("[AI] Calling Gemini...");
                String prompt = """
                        You are an expert AI Career Mentor and Technical Placement Advisor in the JobTracker platform.
                        Directly and comprehensively answer the user's inquiry, career question, or roadmap request with structured, practical advice.
                        Rules:
                        - Directly answer the question asked.
                        - Do NOT return generic canned responses or tell the user to visit other tabs.
                        - If the user asks for a roadmap or transition plan, provide a clear, step-by-step roadmap tailored to the requested role and technologies.
                        - Maintain domain neutrality; focus strictly on the role and skills requested by the user.

                        User Query:
                        %s
                        """.formatted(cleanPrompt);

                String response = geminiService.askGemini(prompt);
                if (response != null && !response.isBlank()) {
                    log.info("[AI] Gemini response received");
                    log.info("[AI] Response source: GEMINI");
                    return new ChatResponse(response.trim(), "GEMINI");
                }
            } catch (GeminiException ex) {
                log.error("[AI] Gemini API error for Career Chat: {}", ex.getMessage());
                throw ex;
            } catch (Exception ex) {
                log.warn("[AI] Gemini call failed for Career Chat: {}. Response source: FALLBACK", ex.getMessage());
            }
        } else {
            log.info("[AI] Reason: API key unavailable");
        }

        log.info("[AI] Response source: FALLBACK");
        String fallbackAnswer = generateDomainNeutralCareerAnswer(cleanPrompt);
        return new ChatResponse(fallbackAnswer, "FALLBACK");
    }

    private String extractPromptText(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) return "";
        String trimmed = rawBody.trim();
        if (trimmed.startsWith("{") && (trimmed.contains("\"message\"") || trimmed.contains("\"prompt\""))) {
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                if (node.has("message") && !node.get("message").asText().isBlank()) {
                    return node.get("message").asText().trim();
                }
                if (node.has("prompt") && !node.get("prompt").asText().isBlank()) {
                    return node.get("prompt").asText().trim();
                }
            } catch (Exception ignored) {
            }
        }
        return trimmed;
    }

    private String generateDomainNeutralCareerAnswer(String query) {
        String lower = query.toLowerCase();

        // 1. Roadmap & Career Transition Queries
        boolean isRoadmap = lower.contains("roadmap") || lower.contains("road map") || lower.contains("path")
                || lower.contains("how to become") || lower.contains("learn") || lower.contains("transition")
                || lower.contains("career guide") || lower.contains("how do i start") || lower.contains("curriculum");

        if (isRoadmap) {
            // Fullstack Developer
            if (lower.contains("fullstack") || lower.contains("full stack") || lower.contains("mern") || lower.contains("mean")) {
                return """
                        ### Full Stack Developer Career Roadmap

                        Here is a structured, end-to-end roadmap to master Full Stack Web Development:

                        #### Phase 1: Modern Frontend Foundations
                        - **Core Web Technologies**: Semantic HTML5, modern CSS (Flexbox, CSS Grid, CSS Variables, Responsive Design), and modern JavaScript (ES6+, Async/Await, Closures, DOM).
                        - **Frontend Frameworks**: Master React or Next.js (Components, Hooks, State Management with Zustand/Redux Toolkit, Server Components).
                        - **Type Safety**: Integrate TypeScript across all client components.

                        #### Phase 2: Backend Architecture & APIs
                        - **Server-Side Runtime**: Node.js/Express, Python (FastAPI/Django), or Java (Spring Boot).
                        - **API Development**: Design RESTful APIs, status codes, input validation, and GraphQL/WebSockets.
                        - **Authentication & Security**: JWT authentication, OAuth2, session management, CORS, and rate limiting.

                        #### Phase 3: Databases & Caching
                        - **Relational Databases (SQL)**: PostgreSQL or MySQL (Schema design, indexing, foreign keys, ACID transactions, query tuning).
                        - **NoSQL & Document Stores**: MongoDB for unstructured/flexible schema needs.
                        - **In-Memory Caching**: Redis for caching expensive queries and session storage.

                        #### Phase 4: DevOps, Cloud & Deployment
                        - **Containerization**: Dockerize client and server applications.
                        - **CI/CD Pipelines**: Automated test and deploy pipelines using GitHub Actions.
                        - **Cloud Hosting**: Deploy full-stack apps to AWS, GCP, Vercel, or Dockerized VPS.
                        """;
            }

            // Backend Developer
            if (lower.contains("backend") || lower.contains("back-end") || lower.contains("server") || lower.contains("java developer") || lower.contains("go developer")) {
                return """
                        ### Comprehensive Backend Developer Career Roadmap

                        Here is a structured, production-grade roadmap to master backend software engineering:

                        #### Phase 1: Core Programming & System Fundamentals
                        - **Language Proficiency**: Master a primary backend language (e.g. Java, Python, Go, Node.js, or C#) focusing on concurrency, memory management, and OOP/functional principles.
                        - **Data Structures & Algorithms**: Arrays, hash maps, trees, graphs, sorting, searching, and time/space complexity (Big-O).

                        #### Phase 2: Web Architectures & APIs
                        - **Protocol Deep-Dive**: HTTP/HTTPS, RESTful API design principles, status codes, and JSON serialization.
                        - **Advanced Communication**: WebSockets for real-time bidirectional data; gRPC/Protobuf for high-performance microservice communication.
                        - **API Security**: Authentication & Authorization (OAuth2, JWT, RBAC), rate limiting, and input sanitization.

                        #### Phase 3: Databases & Caching
                        - **Relational Databases (RDBMS)**: PostgreSQL or MySQL (Schema design, B-Tree indexing, ACID transactions, EXPLAIN query optimization).
                        - **NoSQL & Document Stores**: MongoDB or Cassandra for high-write or document workloads.
                        - **In-Memory Caching**: Redis for caching hot data, session stores, and distributed locking.

                        #### Phase 4: System Architecture & Distributed Systems
                        - **Microservices & Messaging**: Decoupled event-driven architecture using Apache Kafka, RabbitMQ, or AWS SQS.
                        - **Scalability Principles**: Load balancing (Nginx, HAProxy), horizontal scaling, database connection pooling, and read replicas.

                        #### Phase 5: Cloud & DevOps Deployment
                        - **Containerization**: Docker container builds and Kubernetes cluster management.
                        - **CI/CD Automation**: GitHub Actions or GitLab CI automated test/deployment pipelines.
                        """;
            }

            // Frontend Developer
            if (lower.contains("frontend") || lower.contains("front-end") || lower.contains("react") || lower.contains("ui") || lower.contains("angular") || lower.contains("vue")) {
                return """
                        ### Modern Frontend Developer Career Roadmap

                        Here is a roadmap to excel as a modern frontend software engineer:

                        #### Phase 1: Foundations
                        - **Core Web Tech**: Semantic HTML5, modern CSS (Flexbox, CSS Grid, custom properties, responsive design), and modern JavaScript (ES6+, Async/Await, DOM manipulation).
                        - **TypeScript**: Static typing, interfaces, generics, and strict mode development.

                        #### Phase 2: Modern Frameworks & Component Architecture
                        - **React / Next.js**: Component lifecycle, custom hooks, Server Components (RSC), routing, and state management.
                        - **Styling Systems**: Tailwind CSS, CSS Modules, or Styled Components.

                        #### Phase 3: Performance, Testing & State Management
                        - **State Management**: Zustand, Redux Toolkit, or TanStack Query / React Query for server state caching.
                        - **Testing**: Unit and integration testing with Jest and React Testing Library, plus E2E with Playwright.
                        - **Web Performance**: Core Web Vitals (LCP, INP, CLS), code splitting, lazy loading, and bundle analysis.

                        #### Phase 4: Production Deployment & CI/CD
                        - **Build Tools & Deployments**: Vite, Turbopack, and automated deployment platforms like Vercel or AWS CloudFront/S3.
                        """;
            }

            // Python / Data Science / ML Developer
            if (lower.contains("python") || lower.contains("data") || lower.contains("machine learning") || lower.contains("ai") || lower.contains("ml")) {
                return """
                        ### Python & Data Science / Machine Learning Roadmap

                        1. **Python Core & Computing Foundations**: Master Python 3, object-oriented design, virtual environments, NumPy numerical computing, and Pandas dataframes.
                        2. **Data Wrangling & SQL**: Advanced SQL queries, window functions, data cleaning pipelines, and exploratory data analysis (Matplotlib, Seaborn).
                        3. **Classical Machine Learning**: Scikit-Learn algorithms (Linear/Logistic Regression, Random Forests, XGBoost, Clustering, PCA) and cross-validation metrics.
                        4. **Deep Learning & Neural Networks**: PyTorch or TensorFlow, CNNs for computer vision, Transformers for NLP, and Hugging Face models.
                        5. **MLOps & Production Deployment**: Packaging models with FastAPI, containerizing with Docker, MLflow experiment tracking, and cloud model endpoints.
                        """;
            }

            // DevOps / Cloud / SRE
            if (lower.contains("devops") || lower.contains("cloud") || lower.contains("sre") || lower.contains("aws") || lower.contains("azure")) {
                return """
                        ### DevOps & Cloud Engineer Roadmap

                        1. **Operating Systems & Scripting**: Linux command-line administration, Bash scripting, Python automation, and networking fundamentals (TCP/IP, DNS, SSL/TLS, VPCs).
                        2. **Containerization & Orchestration**: Docker multi-stage image builds and Kubernetes cluster architecture (Pods, Deployments, Services, Ingress, Helm).
                        3. **Infrastructure as Code (IaC)**: Terraform / OpenTofu and Ansible configuration automation.
                        4. **CI/CD Pipelines**: GitHub Actions, GitLab CI, or ArgoCD for GitOps deployment pipelines.
                        5. **Observability & Cloud Monitoring**: Prometheus, Grafana, OpenTelemetry, ELK/Loki log aggregation, and SLI/SLO tracking.
                        """;
            }

            // Customer Support / Customer Success
            if (lower.contains("customer") || lower.contains("support") || lower.contains("success") || lower.contains("help desk") || lower.contains("client service")) {
                return """
                        ### Career Roadmap for Customer Support & Customer Success

                        Here is a structured progression roadmap for Customer Support / Customer Success professionals:

                        #### Phase 1: Core Communication & Ticket Resolution
                        - **Omnichannel Support**: Master ticketing systems (Zendesk, Freshdesk, Intercom, Salesforce Service Cloud).
                        - **De-escalation & Empathy**: Develop active listening, clear asynchronous written communication, and conflict de-escalation frameworks.
                        - **Technical Troubleshooting**: Basic networking, browser dev tools, inspecting API status codes, and issue reproduction.

                        #### Phase 2: Process Optimization & Knowledge Management
                        - **Knowledge Base Creation**: Author standard operating procedures (SOPs), customer-facing help articles, and internal troubleshooting macros.
                        - **SLA & Metric Mastery**: Track and optimize First Response Time (FRT), Customer Satisfaction (CSAT), Resolution Time, and Net Promoter Score (NPS).

                        #### Phase 3: Transition to Technical Support or Customer Success Management (CSM)
                        - **Customer Onboarding & Retention**: Guide new clients through implementation, product adoption, and health score monitoring.
                        - **Churn Prevention & Upselling**: Identify expansion opportunities, analyze usage analytics, and run Quarterly Business Reviews (QBRs).
                        - **Technical Integration Skills**: Learn SQL querying, Postman API testing, and webhook configurations to transition toward Technical Account Management (TAM).
                        """;
            }

            return """
                    ### Career Progression Roadmap for %s

                    1. **Core Domain Fundamentals**: Master foundational domain concepts, industry terminology, and essential toolchains for %s.
                    2. **Hands-On Project Application**: Build end-to-end portfolio projects demonstrating practical problem solving and domain deliverables.
                    3. **Standard Industry Tooling**: Learn collaborative version control (Git), automated testing, and CI/CD quality assurance workflows.
                    4. **System & Architecture Depth**: Study domain trade-offs, scalability, and performance optimization.
                    5. **Professional Presentation**: Tailor your ATS resume with quantifiable metrics and prepare scenario-based interview stories.
                    """.formatted(query, query);
        }

        // 2. AWS / Cloud / Technical Interview Preparation Queries
        if (lower.contains("aws") && lower.contains("interview")) {
            return """
                    ### Strategic AWS & Cloud Interview Preparation Guide

                    Here is a targeted guide to prepare for AWS and Cloud Engineering interviews:

                    #### 1. Core AWS Services & Architecture Depth
                    - **Compute**: EC2 instance types, Auto Scaling Groups, Lambda serverless triggers/concurrency, ECS vs EKS container orchestration.
                    - **Storage & Databases**: S3 storage tiers, lifecycle policies, EBS volumes, RDS multi-AZ failover, DynamoDB partition keys and global secondary indexes.
                    - **Networking & Security**: VPC design, Public/Private subnets, NAT Gateways, Security Groups vs NACLs, Route 53, CloudFront CDN, and IAM least-privilege roles.

                    #### 2. Architectural Design Patterns (Well-Architected Framework)
                    - **High Availability & Fault Tolerance**: Multi-Region replication, Active-Active vs Active-Passive failover, and decoupled queues with SQS/SNS.
                    - **Security & Compliance**: Encryption at rest (KMS) and in transit (TLS), AWS WAF, GuardDuty, and CloudTrail auditing.
                    - **Cost Optimization**: Reserved instances, Savings Plans, S3 Intelligent-Tiering, and right-sizing workloads.

                    #### 3. Behavioral Scenarios (STAR Method)
                    - Prepare concrete stories describing production outages, zero-downtime migrations, cost reduction achievements, and disaster recovery testing.
                    """;
        }

        if (lower.contains("interview") || lower.contains("question") || lower.contains("mock") || lower.contains("behavioral")) {
            return """
                    ### Technical & Behavioral Interview Preparation Guide

                    - **Behavioral Questions (STAR Method)**:
                      - **Situation**: Context of the project or challenge.
                      - **Task**: What your specific responsibility was.
                      - **Action**: The exact technical and interpersonal actions you took.
                      - **Result**: Quantifiable business or engineering outcome (e.g., "reduced latency by 40%").

                    - **Technical & System Design Strategy**:
                      - Clarify requirements and constraints (scale, latency, throughput) before writing code or drawing diagrams.
                      - Discuss trade-offs (e.g., SQL vs NoSQL, synchronous vs asynchronous messaging) rather than just stating choices.
                      - Communicate your thinking out loud continuously.
                    """;
        }

        // 3. Resume / ATS Optimization Queries (e.g. Data Scientist, Software Engineer)
        if (lower.contains("resume") || lower.contains("ats") || lower.contains("keyword") || lower.contains("cv")) {
            if (lower.contains("data scientist") || lower.contains("data science") || lower.contains("ml")) {
                return """
                        ### Data Scientist Resume Optimization Guide

                        Here are targeted recommendations to make your Data Science resume stand out to recruiters and ATS parsers:

                        1. **Emphasize Business Impact over Model Names**:
                           - *Weak*: "Trained a machine learning model using Random Forest."
                           - *Strong*: "Built customer churn prediction model with XGBoost, achieving 0.89 AUC-ROC and reducing annual churn by $450k."
                        2. **Demonstrate End-to-End Pipeline Ownership**:
                           - Highlight data extraction (SQL), feature engineering, model validation, and deployment (FastAPI, Docker, AWS Sagemaker).
                        3. **Categorize Technical Skills Clearly**:
                           - **Languages**: Python, SQL, R
                           - **Frameworks/Libraries**: PyTorch, Scikit-Learn, Pandas, NumPy, XGBoost
                           - **Tools & Platforms**: Git, Docker, MLflow, AWS/GCP, Snowflake, Tableau
                        4. **Include Quantifiable Project Metrics**:
                           - Highlight dataset size, latency improvements, inference throughput, and measurable business ROI.
                        """;
            }

            return """
                    ### High-Impact ATS Resume Optimization Guidelines

                    1. **Quantifiable Bullet Points**: Use the Google XYZ formula: *"Accomplished [X], as measured by [Y], by doing [Z]"*.
                    2. **Keyword Alignment**: Include technical competencies, tools, and methodologies that directly match your target job descriptions.
                    3. **Clean ATS Formatting**: Avoid multi-column layouts, tables, embedded graphics, or text boxes that confuse automated parsers.
                    4. **Section Structure**: Summary -> Skills -> Experience -> Projects -> Education & Certifications.
                    """;
        }

        // 4. Dynamic Domain-Neutral Career Advisor Fallback
        return """
                ### Career Advisory & Guidance for: "%s"

                Here is tailored, domain-neutral guidance to help you navigate and excel in this area:

                - **Core Competencies & Tooling**: Identify the standard frameworks, tools, and workflows for %s and focus on end-to-end practical mastery.
                - **Hands-On Deliverables**: Build tangible projects or practical case studies that clearly demonstrate your problem-solving abilities.
                - **Targeted Application & Positioning**: Tailor your resume summary, technical skills section, and behavioral interview examples to align directly with the specific requirements of target positions.

                *(Note: Provide a Gemini API key to unlock fully interactive, generative AI mentoring and custom-tailored step-by-step career pathways.)*
                """.formatted(query, query);
    }
}