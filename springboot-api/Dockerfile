FROM gradle:8-jdk17 AS build

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -g 1001 appgroup \
    && useradd -u 1001 -g appgroup -s /usr/sbin/nologin appuser \
    && mkdir -p /app/files /app/logs /app/uploads \
    && chown -R appuser:appgroup /app

WORKDIR /app

COPY --from=build --chown=appuser:appgroup /home/gradle/src/build/libs/*.jar /app/church-management-api.jar
COPY --from=build --chown=appuser:appgroup /home/gradle/src/app/files /app/files

VOLUME ["/app/logs", "/app/uploads"]

ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV UPLOAD_PATH=/app/uploads
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/church-management-api.jar"]
