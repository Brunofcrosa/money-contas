# ==============================================================
# Dockerfile — Back-end (Spring Boot)
# Multi-stage build: maven builder → JRE runtime (alpine)
# ==============================================================

# ── Stage 1: Build ────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 1. Copy only the POM first to resolve & cache dependencies
#    This layer is only invalidated when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# 2. Copy the rest of the source and build the fat JAR
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Security: run as a dedicated non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Bring only the executable artifact from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Give the new user ownership
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# Use exec form so the JVM receives OS signals properly (graceful shutdown)
ENTRYPOINT ["java", "-jar", "app.jar"]
