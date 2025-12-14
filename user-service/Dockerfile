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

# -------- RUNTIME STAGE --------
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port used by user-service
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
