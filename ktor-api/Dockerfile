FROM gradle:8-jdk19 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:19-jdk-alpine
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/rfm.ktor-server.jar
COPY .env /app/.env
ENTRYPOINT ["java", "-jar", "/app/rfm.ktor-server.jar"]
