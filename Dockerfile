# ═══════════════════════════════════════════════════════════════════════
# ACMAFER — Dockerfile (Spring Boot + Maven)
# ═══════════════════════════════════════════════════════════════════════

# --- Etapa 1: Construcción ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cachear dependencias primero
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar el código fuente y construir el JAR (sin correr tests en el build de imagen)
COPY src src
RUN mvn -B clean package -DskipTests

# --- Etapa 2: Ejecución ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd -m acmafer
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/uploads && chown -R acmafer:acmafer /app
USER acmafer

# Render asigna dinámicamente el puerto vía la variable PORT
ENV PORT=10000
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]