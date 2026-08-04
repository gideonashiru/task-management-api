# Stage 1: build
FROM gradle:jdk25 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# Stage 2: run
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]