# -------- BUILD STAGE --------
FROM maven:3.9.9-eclipse-temurin-23 AS build
WORKDIR /app

# Copy only the pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Pre-fetch dependencies (cached unless pom.xml changes)
RUN mvn dependency:resolve -DskipTests

# Now copy the full source code
COPY src ./src

# Build the application
RUN mvn -B clean package -DskipTests

# -------- EXTRACT LAYERS --------
FROM eclipse-temurin:23-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

COPY --from=extract /app/extracted/dependencies/ ./
COPY --from=extract /app/extracted/spring-boot-loader/ ./
COPY --from=extract /app/extracted/snapshot-dependencies/ ./
COPY --from=extract /app/extracted/application/ ./

ENTRYPOINT ["java", "-jar", "app.jar"]
