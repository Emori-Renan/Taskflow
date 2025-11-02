# 🚀 TaskFlow — Modular Microservices Backend

**TaskFlow** is a scalable, modular backend system for project and task management, built with **Spring Boot 3.5** and **Java 23**. It follows a hybrid **modular monolith/microservice** architecture, emphasizing **Hexagonal Architecture (Ports & Adapters)** and **Domain-Driven Design (DDD)** to ensure loose coupling, high testability, and a clear path for future microservice decomposition.

---

## 🏗️ Architecture & Tech Stack

**Language:** Java 23  
**Framework:** Spring Boot 3.5  
**Security:** Spring Security + JWT  
**API Gateway:** Spring Cloud Gateway  
**Database:** PostgreSQL 15  
**Build Tool:** Maven  
**Containerization:** Docker & Docker Compose  

---

## 🧩 Project Modules

Each module is fully containerized and represents a distinct business domain:

- 🔐 **auth-service** — Manages user registration, login, and JWT token issuance (Port: `8081`)  
- 🌐 **gateway-service** — Public entry point. Handles routing, throttling, and JWT validation (Port: `8080`)  

---

## 🧱 Clean Architecture Principles

Each service follows the **Hexagonal Architecture** pattern:

- **Domain Layer:** Core business entities and logic  
- **Application Layer:** DTOs, ports (interfaces), and service use cases  
- **Infrastructure Layer:** External adapters, persistence, and configuration  

---

## ⚙️ Getting Started with Docker (Recommended)

### 1. Clone the Repository

```bash
git clone <YOUR_REPO_URL>
cd TaskFlow
```

### 2. Build and Start the Environment

```bash
# Build all custom service images
docker compose build

# Start the database and services in detached mode
docker compose up -d
```

### 3. Access the Services

- API Gateway → `http://localhost:8080`  
- Auth Service → `http://localhost:8081`  
- PostgreSQL → `localhost:5432`  

---

## 💻 Local Development & Testing

### Build Commands

```bash
# Build all services
mvn clean package

# Build a single service
cd auth-service/
mvn clean package
```

### Run Tests

```bash
cd auth-service
mvn test
```

---

## 🐳 Useful Docker Commands

```bash
# Start containers in the foreground
docker compose up

# Stop and remove containers, networks, and volumes
docker compose down

# View logs for a specific container
docker logs auth-service
```

---

## 💡 Future Roadmap

Planned modules include:

- 👤 `user-service` — User profiles and settings  
- 📁 `project-service` — Projects, teams, and permissions  
- 🧾 `task-service` — Task tracking and time logging  
- 🔔 `notification-service` — Event-based messaging and updates  

---

## 👨‍💻 Author

**Renan Emori**  
Building modular and scalable backend systems.  
Focused on clean architecture and cloud-native design.  
📍 Based in Japan 🇯🇵  
