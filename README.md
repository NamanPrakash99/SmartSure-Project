<div align="center">

# 🛡️ SmartSure — Insurance Management System (Backend Services)

### *Smart Insurance for a Modern World*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Redis](https://img.shields.io/badge/Redis-Caching-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-Microservices-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/cloud)

<br/>

> A production-grade **Insurance Management Platform Backend** built with **Spring Boot Microservices**. Featuring JWT authentication, role-based access control, distributed caching, and event-driven processing.

</div>

---

## 📑 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Microservices Breakdown](#-microservices-breakdown)
- [Security Architecture](#-security-architecture)
- [Inter-Service Communication](#-inter-service-communication)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Endpoints](#-api-endpoints)
- [Contributors](#-contributors)

---

## 🏛️ Architecture Overview

SmartSure follows a **Microservices Architecture** pattern, where each business domain is encapsulated into an independently deployable service. The services communicate through an API Gateway with service discovery.

```
┌──────────────────────────────────────────────────────────────────────┐
│                    🌐 API GATEWAY (:8888)                            │
│             Spring Cloud Gateway • JWT Validation                    │
│                 Route Filtering • CORS Handling                      │
└──────┬────────┬────────┬────────┬────────────────────────────────────┘
       │        │        │        │
       ▼        ▼        ▼        ▼
   ┌───────┐┌───────┐┌───────┐┌───────┐
   │ Auth  ││Policy ││Claims ││ Admin │
   │Service││Service││Service││Service│
   │ :8002 ││ :8004 ││ :8003 ││ :8005 │
   └───┬───┘└───┬───┘└───┬───┘└───┬───┘
       │        │        │        │
       └────────┴────────┴────────┘
                 │
          ┌──────┼──────┐
          ▼      ▼      ▼
    ┌──────────┐┌────────┐┌───────┐
    │PostgreSQL││RabbitMQ││ Redis │
    │  :5432   ││ :5672  ││ :6379 │
    └──────────┘└────────┘└───────┘
```

---

## 🛠️ Tech Stack

### Backend

| Technology | Purpose |
|:---|:---|
| ![Java](https://img.shields.io/badge/-Java%2017-ED8B00?style=flat-square&logo=openjdk&logoColor=white) | Core language |
| ![Spring Boot](https://img.shields.io/badge/-Spring%20Boot%203.4-6DB33F?style=flat-square&logo=springboot&logoColor=white) | Microservice framework |
| ![Spring Cloud](https://img.shields.io/badge/-Spring%20Cloud-6DB33F?style=flat-square&logo=spring&logoColor=white) | Eureka, Gateway, OpenFeign |
| ![Spring Security](https://img.shields.io/badge/-Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) | Authentication & Authorization |
| ![PostgreSQL](https://img.shields.io/badge/-PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white) | Relational database |
| ![RabbitMQ](https://img.shields.io/badge/-RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white) | Async messaging (event-driven) |
| ![Redis](https://img.shields.io/badge/-Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | Distributed caching |
| ![Maven](https://img.shields.io/badge/-Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white) | Build & dependency management |

---

## 🧩 Microservices Breakdown

### 🔐 Auth Service (`:8002`)
Handles the complete authentication lifecycle.
- User registration with OTP verification
- JWT access token & refresh token generation
- Password reset via email (token-based)
- Redis-backed session management

### 📋 Policy Service (`:8004`)
Manages insurance policy templates and user policy subscriptions.
- CRUD for insurance policy templates (Admin)
- Policy type management
- Policy purchase & renewal workflows
- User-specific policy tracking
- Aggregate policy statistics

### 📁 Claims Service (`:8003`)
Processes and manages insurance claims with document support.
- File claims with document upload
- Claim status lifecycle (`SUBMITTED` → `UNDER_REVIEW` → `APPROVED`/`REJECTED` → `CLOSED`)
- RabbitMQ listener for claim review events
- Document storage and retrieval

### 👥 Admin Service (`:8005`)
Aggregates data across microservices for the admin dashboard.
- OpenFeign clients for cross-service communication
- Spring Retry with `@Recover` fallbacks
- User management & statistics aggregation
- RabbitMQ event listener

### 🌐 API Gateway (`:8888`)
The single entry point for all client requests.
- Dynamic routing to microservices via Eureka
- JWT validation & role extraction
- Route-level security filtering

### 📡 Eureka Server (`:8761`)
Netflix Eureka-based service discovery registry.
- Automatic service registration & deregistration
- Health monitoring for all services

---

## 🔐 Security Architecture

SmartSure implements a multi-layered security model across the backend services.

```
┌──────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                       │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Layer 1: API Gateway Filter                             │
│  ├─ JWT signature verification                           │
│  ├─ Role extraction & route validation                   │
│  └─ Rejects invalid/expired tokens with 403              │
│                                                          │
│  Layer 2: Service-Level Security                         │
│  ├─ GatewaySecurityFilter on each microservice           │
│  └─ Validates X-User-Id and X-User-Role headers          │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### Token Lifecycle

```
Client Login → Auth Service generates Access Token (15 min) + Refresh Token (days)
     │
     ├─ Every API call → Client attaches Bearer token
     │
     ├─ Token expires → 401 received
     │     │
     │     └─ Client requests new Token via /auth/refresh-token
     │
     └─ Refresh Token expires → Full login required
```

---

## 🔗 Inter-Service Communication

| Pattern | Technology | Use Case |
|:---|:---|:---|
| **Synchronous** | Spring Cloud OpenFeign | Admin Service ↔ Policy/Claims Service |
| **Asynchronous** | RabbitMQ | Claim review events, email notifications |
| **Caching** | Redis | Session data, frequently accessed policies |

### Resilience Strategy
```java
@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
public PolicyStatsDto getPolicyStats() {
    return policyFeignClient.getStats(); // Cross-service call
}

@Recover
public PolicyStatsDto recoverPolicyStats(Exception e) {
    return PolicyStatsDto.defaultFallback(); // Graceful degradation
}
```

---

## 📁 Project Structure

```
SmartSure-Insurance-Management-System/
│
├── 🌐 api-gateway/          # Spring Cloud Gateway (JWT + Routing)
├── 🔐 auth-service/         # Authentication & Authorization
├── 📋 policy-service/       # Policy Management (CQRS)
├── 📁 claims-service/       # Claims Processing
├── 👥 admin-service/        # Admin Aggregation (Feign + Retry)
├── 📡 eureka-server/        # Service Discovery
│
├── pom.xml                  # Maven parent POM (multi-module)
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|:---|:---|
| Java | 17+ |
| Maven | 3.x |
| PostgreSQL | 15+ |
| RabbitMQ | Latest |
| Redis | Latest |

### Startup Sequence

Ensure PostgreSQL, RabbitMQ, and Redis are running locally or via Docker before starting the services.

Start services in this exact order:

```bash
# 1. Control Plane
cd eureka-server && mvn spring-boot:run    # :8761

# 2. Application Services
cd auth-service && mvn spring-boot:run     # :8002
cd policy-service && mvn spring-boot:run   # :8004
cd claims-service && mvn spring-boot:run   # :8003
cd admin-service && mvn spring-boot:run    # :8005

# 3. Gateway
cd api-gateway && mvn spring-boot:run      # :8888
```

### Testing

Run the test suite across all services using Maven:

```bash
mvn test
```

> [!NOTE]
> The test suite has been heavily optimized to be lean and fast. We have intentionally removed unnecessary boilerplate (e.g., default `*ApplicationTests`) and excessively verbose edge cases across our core services (`auth-service`, `claims-service`, `policy-service`, and `admin-service`). This ensures our tests remain maintainable and focused on critical business logic and happy paths.

---

## 🔌 API Endpoints

### Auth Service (`/auth-service/api/auth`)
| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/register` | Register a new user |
| `POST` | `/login` | Authenticate & receive JWT |
| `POST` | `/refresh-token` | Refresh access token |
| `POST` | `/forgot-password` | Request password reset email |
| `POST` | `/reset-password` | Reset password with token |

### Policy Service (`/policy-service/api`)
| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/policies` | List all available policies |
| `GET` | `/policies/:id` | Get policy details |
| `POST` | `/policies/purchase` | Purchase a policy |
| `POST` | `/policies/renew/:id` | Renew a policy |
| `POST` | `/admin/policies` | Create policy template (Admin) |
| `PUT` | `/admin/policies/:id` | Update policy template (Admin) |
| `DELETE` | `/admin/policies/:id` | Delete policy template (Admin) |

### Claims Service (`/claims-service/api`)
| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/claims` | File a new claim |
| `GET` | `/claims/user/:userId` | Get user's claims |
| `GET` | `/claims/:id` | Get claim details |
| `PUT` | `/claims/:id/review` | Review claim (Admin) |

---

## 👨‍💻 Contributors

<a href="https://github.com/NamanPrakash99">
  <img src="https://img.shields.io/badge/Naman%20Prakash-Developer-6366f1?style=for-the-badge&logo=github&logoColor=white" />
</a>

---

<div align="center">

**Built with ❤️ using Spring Boot**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)

⭐ Star this repository if you found it useful!

</div>
