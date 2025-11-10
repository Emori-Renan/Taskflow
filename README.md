# 🚀 TaskFlow — Modular Microservices Backend

**TaskFlow** is a scalable, modular backend system for **Trello-like project and task management**, built with **Spring Boot 3.5** and **Java 23**. It follows a hybrid **modular monolith/microservice** architecture, emphasizing **Hexagonal Architecture (Ports & Adapters)** and **Domain-Driven Design (DDD)** to ensure loose coupling, high testability, and a clear path for future microservice decomposition.

---

## 🧠 Project Overview

TaskFlow is designed to support collaborative project and task management, inspired by tools like **Trello**. It enables users to organize work into boards, lists, and cards through a set of specialized microservices. Each service handles a distinct domain and communicates via REST and asynchronous messaging (SNS/SQS via LocalStack). Here's how the full system is intended to work:

### 🔐 `auth-service`
Handles user authentication and authorization using JWT.  
Includes rate limiting via Redis to prevent abuse and brute-force attempts.

### 👤 `user-service`
Manages user profiles, preferences, and account settings.  
Integrates with `auth-service` to secure endpoints and enforce access control.

### 📁 `project-service`
Acts as the **board engine**, allowing users to create and manage boards (projects), define columns (lists), and assign team members.  
Supports permission-based access, role management, and board-level collaboration.

### 🧾 `task-service`
Functions as the **card engine**, managing tasks that live inside project columns.  
Supports task creation, assignment, movement between columns, due dates, labels, and time tracking.

### 🔔 `notification-service`
Handles asynchronous event delivery using SNS/SQS.  
Sends real-time notifications for task updates, board changes, and user mentions.

### 🌐 `gateway-service`
Acts as the unified entry point for all services.  
Handles routing, global rate limiting, and JWT validation to secure traffic.

Together, these services form a cohesive system for managing teams, boards, and tasks with real-time feedback and secure access—just like a modular, backend-powered Trello.

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

## 🧱 Clean Architecture Principles

Each service follows the **Hexagonal Architecture** pattern:

- **Domain Layer:** Core business entities and logic  
- **Application Layer:** DTOs, ports (interfaces), and service use cases  
- **Infrastructure Layer:** External adapters, persistence, and configuration  

---

## ⚙️ Getting Started with Docker (Recommended)

### 1. Clone the Repository

```bash
git clone https://github.com/Emori-Renan/Taskflow
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

## 📋 To-Do Checklist

Track your progress as you build out the system:

- [x] `auth-service` — JWT authentication and Redis rate limiting  
- [x] `gateway-service` — API gateway and request routing  
- [x] Containerization — Docker setup for all services  
- [ ] `user-service` — User profile management  
- [ ] `project-service` — Board and column management (Trello-style)  
- [ ] `task-service` — Task/card tracking and movement  
- [ ] `notification-service` — Event-driven messaging via SNS/SQS  
- [ ] CI/CD pipeline — Automated builds and deployments  
- [ ] LocalStack integration — AWS emulation for development  
- [ ] Monitoring & Logging — Centralized logs and metrics  

---

## 👨‍💻 Author

**Renan Emori**  
Building modular and scalable backend systems.  
Focused on clean architecture and cloud-native design.  
📍 Based in Japan 🇯🇵  
