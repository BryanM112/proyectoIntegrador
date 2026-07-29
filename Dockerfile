# syntax=docker/dockerfile:1.7

# ============================
# Etapa 1: compilación
# ============================
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /workspace/app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew clean bootJar -x test --no-daemon

# ============================
# Etapa 2: ejecución
# ============================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=builder \
    /workspace/app/build/libs/proyectointegrador-0.0.1-SNAPSHOT.jar \
    /app/app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]