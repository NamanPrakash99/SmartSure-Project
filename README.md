<div align="center">

# 🛡️ SmartSure — Insurance Management System

### *Smart Insurance for a Modern World*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-6.0-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Alpine-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600?style=flat-square&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Redis](https://img.shields.io/badge/Redis-Caching-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![Razorpay](https://img.shields.io/badge/Razorpay-Payments-0C2451?style=flat-square&logo=razorpay&logoColor=white)](https://razorpay.com/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3.4-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)
[![Redux Toolkit](https://img.shields.io/badge/Redux_Toolkit-2.11-764ABC?style=flat-square&logo=redux&logoColor=white)](https://redux-toolkit.js.org/)
[![Vite](https://img.shields.io/badge/Vite-8.0-646CFF?style=flat-square&logo=vite&logoColor=white)](https://vitejs.dev/)

[![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-Microservices-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/cloud)
[![Gemini AI](https://img.shields.io/badge/Gemini_AI-Chatbot-4285F4?style=flat-square&logo=googlegemini&logoColor=white)](https://ai.google.dev/)

<br/>

> A production-grade, full-stack **Insurance Management Platform** built with a **Spring Boot Microservices** backend and a **React + TypeScript** frontend. Featuring JWT authentication with silent token refresh, Razorpay payment integration, AI-powered chatbot, and role-based access control.

</div>

---

## 📑 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [System Architecture Diagram](#-system-architecture-diagram)
- [Tech Stack](#-tech-stack)
- [Key Features](#-key-features)
- [Microservices Breakdown](#-microservices-breakdown)
- [Frontend Architecture](#-frontend-architecture)
- [Security Architecture](#-security-architecture)
- [Payment Flow (Razorpay)](#-payment-flow-razorpay)
- [Inter-Service Communication](#-inter-service-communication)
- [Observability & Monitoring](#-observability--monitoring)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Endpoints](#-api-endpoints)
- [Environment Variables](#-environment-variables)
- [Contributors](#-contributors)

---

## 🏛️ Architecture Overview

SmartSure follows a **Microservices Architecture** pattern, where each business domain is encapsulated into an independently deployable service. All services are orchestrated via Docker Compose and communicate through an API Gateway with service discovery.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT (Browser)                             │
│               React 19 + TypeScript + Redux Toolkit                 │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTPS
                               ▼
┌──────────────────────────────────────────────────────────────────────┐
│                    🌐 API GATEWAY (:8888)                            │
│             Spring Cloud Gateway • JWT Validation                    │
│                 Route Filtering • CORS Handling                      │
└──────┬────────┬────────┬────────┬────────┬───────────────────────────┘
       │        │        │        │        │
       ▼        ▼        ▼        ▼        ▼
   ┌───────┐┌───────┐┌───────┐┌───────┐┌───────┐
   │ Auth  ││Policy ││Claims ││Payment││ Admin │
   │Service││Service││Service││Service││Service│
   │ :8002 ││ :8004 ││ :8003 ││ :8085 ││ :8005 │
   └───┬───┘└───┬───┘└───┬───┘└───┬───┘└───┬───┘
       │        │        │        │        │
       └────────┴────────┴────────┴────────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐
    │PostgreSQL│  │ RabbitMQ │  │  Redis   │
    │  :5432   │  │  :5672   │  │  :6379   │
    └──────────┘  └──────────┘  └──────────┘
```

---

## 🛠️ Tech Stack

### Backend

| Technology | Purpose |
|:---|:---|
| ![Java](https://img.shields.io/badge/-Java%2017-ED8B00?style=flat-square&logo=openjdk&logoColor=white) | Core language |
| ![Spring Boot](https://img.shields.io/badge/-Spring%20Boot%203.4-6DB33F?style=flat-square&logo=springboot&logoColor=white) | Microservice framework |
| ![Spring Cloud](https://img.shields.io/badge/-Spring%20Cloud-6DB33F?style=flat-square&logo=spring&logoColor=white) | Eureka, Config Server, Gateway, OpenFeign |
| ![Spring Security](https://img.shields.io/badge/-Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) | Authentication & Authorization |
| ![PostgreSQL](https://img.shields.io/badge/-PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white) | Relational database |
| ![RabbitMQ](https://img.shields.io/badge/-RabbitMQ-FF6600?style=flat-square&logo=rabbitmq&logoColor=white) | Async messaging (event-driven) |
| ![Redis](https://img.shields.io/badge/-Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | Distributed caching |
| ![Maven](https://img.shields.io/badge/-Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white) | Build & dependency management |
| ![Docker](https://img.shields.io/badge/-Docker-2496ED?style=flat-square&logo=docker&logoColor=white) | Containerization |
| ![Razorpay](https://img.shields.io/badge/-Razorpay-0C2451?style=flat-square&logo=razorpay&logoColor=white) | Payment gateway |

### Frontend

| Technology | Purpose |
|:---|:---|
| ![React](https://img.shields.io/badge/-React%2019-61DAFB?style=flat-square&logo=react&logoColor=black) | UI library |
| ![TypeScript](https://img.shields.io/badge/-TypeScript%206-3178C6?style=flat-square&logo=typescript&logoColor=white) | Type-safe development |
| ![Redux Toolkit](https://img.shields.io/badge/-Redux%20Toolkit-764ABC?style=flat-square&logo=redux&logoColor=white) | Global state management |
| ![Tailwind CSS](https://img.shields.io/badge/-Tailwind%20CSS%203.4-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white) | Utility-first styling |
| ![Vite](https://img.shields.io/badge/-Vite%208-646CFF?style=flat-square&logo=vite&logoColor=white) | Lightning-fast build tool |
| ![React Router](https://img.shields.io/badge/-React%20Router%207-CA4245?style=flat-square&logo=reactrouter&logoColor=white) | Client-side routing |
| ![React Hook Form](https://img.shields.io/badge/-React%20Hook%20Form-EC5990?style=flat-square&logo=reacthookform&logoColor=white) | Performant form handling |
| ![Zod](https://img.shields.io/badge/-Zod-3E67B1?style=flat-square&logo=zod&logoColor=white) | Schema-based form validation |

- 🐳 **Fully Dockerized** — one-command deployment
- 🌙 **Dark Mode** support across the entire UI
- 📱 **Responsive Design** — mobile-first Tailwind CSS

---

## 🧩 Microservices Breakdown

### 🔐 Auth Service (`:8002`)
Handles the complete authentication lifecycle.
- User registration with OTP verification
- JWT access token & refresh token generation
- Password reset via email (token-based)
- Gateway security filter for JWT validation
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

### 💳 Payment Service (`:8085`)
Orchestrates the Razorpay payment flow.
- Create Razorpay orders server-to-server
- Cryptographic payment verification (HMAC SHA256)
- Transaction persistence & status tracking
- Email confirmation via SMTP

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
- CORS configuration
- Route-level security filtering

### 📡 Eureka Server (`:8761`)
Netflix Eureka-based service discovery registry.
- Automatic service registration & deregistration
- Health monitoring for all services

### ⚙️ Config Server (`:9999`)
Spring Cloud Config for centralized configuration.
- Environment-specific configurations
- Runtime config refresh without redeployment

---

## 🎨 Frontend Architecture

The React frontend follows a clean, modular architecture with strict separation of concerns.

```
smartsure-frontend/src/
├── api/                    # Centralized API service layer
│   ├── axios.ts            # Axios instance with JWT interceptors
│   ├── apiErrorHandler.ts  # Global error handling utility
│   ├── authService.ts      # Authentication endpoints
│   ├── policyService.ts    # Policy endpoints (with caching)
│   ├── claimService.ts     # Claims endpoints
│   ├── paymentService.ts   # Razorpay integration
│   ├── adminService.ts     # Admin endpoints
│   └── aiService.ts        # Gemini AI chatbot integration
│
├── components/
│   ├── common/             # 16 reusable UI primitives
│   │   ├── Button.tsx      # Variant-based button system
│   │   ├── Modal.tsx       # Reusable overlay modal
│   │   ├── LoadingSpinner  # Centralized loading indicator
│   │   ├── EmptyState.tsx  # Empty data fallback UI
│   │   ├── Pagination.tsx  # Table pagination component
│   │   ├── StatusBadge.tsx # Dynamic status indicators
│   │   ├── StatsCard.tsx   # Dashboard statistic cards
│   │   ├── Chatbot.tsx     # AI chatbot widget
│   │   ├── FormInput.tsx   # Controlled form inputs
│   │   └── ...             # + 7 more form components
│   └── layout/
│       └── DashboardLayout # Persistent sidebar + header
│
├── context/
│   └── AuthContext.tsx      # Auth state + Redux bridge
│
├── hooks/
│   └── useDebounce.ts      # Debounced search hook
│
├── pages/
│   ├── auth/               # Login, Register, Forgot/Reset Password
│   ├── customer/           # Dashboard, Policies, Claims, Payments
│   ├── admin/              # Dashboard, Policy CRUD, Claims Review, Reports
│   └── public/             # About, Contact, Terms
│
├── routes/
│   └── AppRouter.tsx       # Protected routes + role-based guards
│
├── schemas/                # Zod validation schemas
│   ├── authSchema.ts       # Login/Register validation
│   ├── claimSchema.ts      # Claim form validation
│   └── policySchema.ts     # Policy form validation
│
├── store/                  # Redux Toolkit
│   ├── index.ts            # Store configuration
│   ├── hooks.ts            # Typed useAppSelector/useAppDispatch
│   └── slices/
│       ├── authSlice.ts    # Authentication state
│       └── themeSlice.ts   # Dark mode toggle
│
└── types/
    └── index.ts            # Global TypeScript interfaces
```

---

## 🔐 Security Architecture

SmartSure implements a **multi-layered security model** across both frontend and backend.

```
┌──────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                       │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Layer 1: Frontend Route Guards                          │
│  ├─ <ProtectedRoute allowedRoles={['ADMIN']}>            │
│  └─ Prevents unauthorized component mounting             │
│                                                          │
│  Layer 2: Axios Interceptors                             │
│  ├─ Auto-attaches Bearer JWT on every request            │
│  └─ Silent token refresh on 401 responses                │
│                                                          │
│  Layer 3: API Gateway Filter                             │
│  ├─ JWT signature verification                           │
│  ├─ Role extraction & route validation                   │
│  └─ Rejects invalid/expired tokens with 403              │
│                                                          │
│  Layer 4: Service-Level Security                         │
│  ├─ GatewaySecurityFilter on each microservice           │
│  └─ Validates X-User-Id and X-User-Role headers          │
│                                                          │
└──────────────────────────────────────────────────────────┘
```

### Token Lifecycle

```
User Login → Auth Service generates Access Token (15 min) + Refresh Token (days)
     │
     ├─ Token stored in localStorage & Redux
     │
     ├─ Every API call → Request Interceptor attaches Bearer token
     │
     ├─ Token expires → 401 received
     │     │
     │     ├─ Response Interceptor catches 401
     │     ├─ Silent POST to /auth/refresh-token
     │     ├─ New Access Token obtained
     │     └─ Original request retried transparently
     │
     └─ Refresh Token expires → Full logout & redirect to /login
```

---

## 💳 Payment Flow (Razorpay)

The payment integration follows a secure **three-phase cryptographic handshake** between the frontend, backend, and Razorpay.

```
┌─────────┐         ┌──────────────┐         ┌──────────┐
│  React  │         │Payment Service│        │ Razorpay │
│Frontend │         │  (Spring Boot)│        │  Server  │
└────┬────┘         └──────┬───────┘         └────┬─────┘
     │                      │                      │
     │ 1. Click "Buy Now"   │                      │
     │─────────────────────>│                      │
     │   {userId, policyId, │                      │
     │    amount}           │ 2. Create Order      │
     │                      │─────────────────────>│
     │                      │                      │
     │                      │  3. Return orderId   │
     │                      │<─────────────────────│
     │  4. Return orderId   │                      │
     │<─────────────────────│                      │
     │                      │                      │
     │ 5. Open Razorpay Modal                      │
     │────────────────────────────────────────────>│
     │                      │                      │
     │ 6. Payment Complete  │                      │
     │<────────────────────────────────────────────│
     │  {payment_id,        │                      │
     │   signature}         │                      │
     │                      │                      │
     │ 7. Verify Signature  │                      │
     │─────────────────────>│                      │
     │                      │ 8. HMAC SHA256 verify│
     │                      │─────────────────────>│
     │                      │                      │
     │  9. Verified ✅      │                      │
     │<─────────────────────│                      │
     │                      │                      │
     │ 10. Activate Policy  │                      │
     │─────────────────────>│                      │
     │                      │                      │
     │ 11. Success Toast 🎉 │                      │
     │  Navigate to         │                      │
     │  /my-policies        │                      │
```

---

## 🔗 Inter-Service Communication

| Pattern | Technology | Use Case |
|:---|:---|:---|
| **Synchronous** | Spring Cloud OpenFeign | Admin Service ↔ Policy/Claims Service |
| **Asynchronous** | RabbitMQ | Claim review events, email notifications, payment confirmations |
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
├── ⚙️ config-server/        # Centralized Configuration
├── 🎨 smartsure-frontend/   # React 19 + TypeScript + Vite
│
├── docker-compose.yml       # Container orchestration
├── pom.xml                  # Maven parent POM (multi-module)
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|:---|:---|
| Java | 17+ |
| Node.js | 18+ |
| Docker & Docker Compose | Latest |
| Maven | 3.x |
| PostgreSQL | 15+ (or use Docker) |

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/NamanPrakash99/SmartSure-Insurance-Management-System.git
cd SmartSure-Insurance-Management-System

# Start all 15 containers
docker-compose up --build -d

# Frontend will be available at http://localhost:5173
# API Gateway at http://localhost:8888
```

### Option 2: Manual Startup

**Backend** — Start services in this order:

```bash
# 1. Infrastructure
# Start PostgreSQL, RabbitMQ, Redis (via Docker or locally)

# 2. Control Plane
cd eureka-server && mvn spring-boot:run    # :8761
cd config-server && mvn spring-boot:run    # :9999

# 3. Application Services
cd auth-service && mvn spring-boot:run     # :8002
cd policy-service && mvn spring-boot:run   # :8004
cd claims-service && mvn spring-boot:run   # :8003
cd payment-service && mvn spring-boot:run  # :8085
cd admin-service && mvn spring-boot:run    # :8005

# 4. Gateway
cd api-gateway && mvn spring-boot:run      # :8888
```

**Frontend:**

```bash
cd smartsure-frontend
npm install
npm run dev    # http://localhost:5173
```

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

### Payment Service (`/payment-service/payment`)
| Method | Endpoint | Description |
|:---|:---|:---|
| `POST` | `/create` | Create Razorpay order |
| `POST` | `/verify` | Verify payment signature |

---

## ⚙️ Environment Variables

Key environment variables configured in `docker-compose.yml`:

| Variable | Description | Default |
|:---|:---|:---|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection | `jdbc:postgresql://postgres-db:5432/SmartSure` |
| `SPRING_RABBITMQ_HOST` | RabbitMQ hostname | `rabbitmq` |
| `SPRING_DATA_REDIS_HOST` | Redis hostname | `redis` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka registry URL | `http://eureka-server:8761/eureka/` |
| `SPRING_CONFIG_IMPORT` | Config Server URL | `configserver:http://config-server:9999` |

---

## 👨‍💻 Contributors

<a href="https://github.com/NamanPrakash99">
  <img src="https://img.shields.io/badge/Naman%20Prakash-Developer-6366f1?style=for-the-badge&logo=github&logoColor=white" />
</a>

---

<div align="center">

**Built with ❤️ using Spring Boot & React**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-61DAFB?style=flat-square&logo=react&logoColor=black)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

⭐ Star this repository if you found it useful!

</div>
