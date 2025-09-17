# Multi-stage build for Spring Boot application
FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Build the Spring Boot application
RUN gradle bootJar --no-daemon

# Runtime stage
FROM openjdk:17-jdk-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create app directory and user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup && \
    mkdir -p /app/logs /app/uploads && \
    chown -R appuser:appgroup /app

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY --from=build --chown=appuser:appgroup /home/gradle/src/build/libs/*.jar /app/church-management-api.jar

# Copy static files directory structure
COPY --chown=appuser:appgroup app/files /app/files

# Create volume mount points
VOLUME ["/app/logs", "/app/uploads", "/app/files"]

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/church-management-api.jar"]
