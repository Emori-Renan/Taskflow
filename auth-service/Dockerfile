# ============================================
# Stage 1: Dependencies Cache
# ============================================
FROM eclipse-temurin:23-jdk-alpine AS deps
RUN apk update && apk add maven

WORKDIR /app

# Copy only the service's pom.xml to cache dependencies
COPY pom.xml .

# Pre-fetch dependencies (cached unless pom.xml changes)
RUN mvn dependency:resolve -DskipTests


# ============================================
# Stage 2: Build
# ============================================
FROM deps AS build

# Copy the actual source code
COPY src ./src

# Build the application
RUN mvn -B clean package -DskipTests


# ============================================
# Stage 3: Runtime
# ============================================
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
ENV TZ=Asia/Tokyo

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
