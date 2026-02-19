# Multi-stage build for Spring Boot application
FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

# Build the Spring Boot application
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jdk-jammy

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Create app directory and user for security
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/false appuser && \
    mkdir -p /app/logs /app/uploads && \
    chown -R appuser:appgroup /app

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY --from=build --chown=appuser:appgroup /home/gradle/src/build/libs/*.jar /app/church-management-api.jar

# Create volume mount points
VOLUME ["/app/logs"]

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
