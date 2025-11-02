# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY auth-service /app/auth-service
COPY gateway-service /app/gateway-service
RUN mvn clean package -pl gateway-service -am -DskipTests

# Stage 2: Run
FROM eclipse-temurin:23-jdk
WORKDIR /app

COPY --from=build /app/gateway-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
