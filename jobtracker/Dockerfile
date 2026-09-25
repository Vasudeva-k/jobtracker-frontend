# ===================================================================
# Stage 1: Build Stage (Java 21 JDK & Maven)
# ===================================================================
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy Maven wrapper files and pom.xml first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Ensure maven wrapper executable permissions
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ===================================================================
# Stage 2: Production Runtime Stage (Lightweight Java 21 JRE)
# ===================================================================
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Create a non-privileged user and group for security
RUN groupadd -r spring && useradd -r -g spring spring

# Create uploads directory and set permissions
RUN mkdir -p /app/uploads/resumes && chown -R spring:spring /app

# Copy the compiled JAR artifact from the builder stage
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Switch to non-root user
USER spring

# Render provides the PORT environment variable dynamically at runtime
ENV PORT=8081
EXPOSE ${PORT}

# Run the Spring Boot JAR with container-aware JVM memory settings and Render PORT binding
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-8081} -jar app.jar"]
