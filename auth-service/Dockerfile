# Stage 1: Build with JDK 23
FROM eclipse-temurin:23-jdk-alpine AS build

WORKDIR /app

# Install Maven
RUN apk update && apk add maven

# Copy the entire TaskFlow project
COPY pom.xml /app/pom.xml
COPY auth-service /app/auth-service
COPY gateway-service /app/gateway-service


# Build only auth-service
RUN mvn clean package -pl auth-service -am

# Stage 2: Run with JRE 23
FROM eclipse-temurin:23-jre-alpine

ENV TZ=Asia/Tokyo

COPY --from=build /app/auth-service/target/*.jar auth-service.jar

ENTRYPOINT ["java", "-jar", "/auth-service.jar"]
