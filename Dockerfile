# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Better layer caching: copy pom first, then sources
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src

# Build runnable jar
RUN mvn -q -DskipTests clean package

# ---------- Run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Optional: run as non-root
#RUN useradd -r -u 1001 appuser
#USER appuser

# Copy jar from build stage
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

# Use prod profile by default inside container (can be overridden by compose env)
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java","-jar","/app/app.jar"]