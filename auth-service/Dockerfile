# ============================================
# Stage 1: Dependencies Cache
# ============================================
FROM eclipse-temurin:23-jdk-alpine AS deps
RUN apk update && apk add maven

WORKDIR /app

# Copy parent + service pom so Maven can resolve structure
COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml

# Pre-fetch dependencies (fast rebuilds)
RUN mvn -pl auth-service -am dependency:resolve -DskipTests


# ============================================
# Stage 2: Build
# ============================================
FROM deps AS build

# Now copy actual source (this invalidates cache only on changes)
COPY auth-service/src auth-service/src

RUN mvn -pl auth-service -am clean package -DskipTests


# ============================================
# Stage 3: Runtime
# ============================================
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app
ENV TZ=Asia/Tokyo

COPY --from=build /app/auth-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
