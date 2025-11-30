FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /app

# Copy entire source so Maven can resolve modules
COPY pom.xml pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml

# Install dependencies first (cached)
RUN mvn -pl gateway-service -am dependency:resolve -DskipTests

# Copy code only after dependencies
COPY gateway-service/src gateway-service/src

RUN mvn -pl gateway-service -am clean package -DskipTests

# -------- RUNTIME --------
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
COPY --from=build /app/gateway-service/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
