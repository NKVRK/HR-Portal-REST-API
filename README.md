# HR Portal REST API

A comprehensive **Human Resources Management REST API** built with Spring Boot. It provides secure employee management, department organization, and leave request workflows with JWT-based authentication and role-based access control.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Clone the Repository](#clone-the-repository)
  - [Configure Environment Variables](#configure-environment-variables)
  - [Build and Run](#build-and-run)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication)
  - [Employees](#employees)
  - [Departments](#departments)
  - [Leave Requests](#leave-requests)
- [Authentication Flow](#authentication-flow)
- [Role-Based Access Control](#role-based-access-control)
- [Project Structure](#project-structure)
- [Running Tests](#running-tests)

---

## Features

- 🔐 **JWT Authentication** — Stateless token-based authentication with BCrypt password hashing
- 👥 **Employee Management** — Full CRUD operations for employee profiles
- 🏢 **Department Management** — Organize employees into departments
- 📋 **Leave Request Workflow** — Apply for, approve, and reject leave requests
- 🛡️ **Role-Based Access Control** — `ADMIN` and `EMPLOYEE` roles with fine-grained permission enforcement
- ✅ **Input Validation** — Request validation using Jakarta Bean Validation
- ⚡ **Auto-provisioned Schema** — Hibernate auto-creates and updates database tables on startup

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security + JWT (JJWT 0.12.6) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8+ |
| Test Database | H2 (in-memory) |
| Build Tool | Maven |
| Utilities | Lombok |

---

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8+** running on `localhost:3306`

---

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/NKVRK/HR-Portal-REST-API.git
cd HR-Portal-REST-API
```

### Configure Environment Variables

The application reads its configuration from environment variables with sensible defaults. Set the following variables before starting the server:

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/hr_portal?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true` | JDBC connection URL |
| `DB_USERNAME` | `root` | Database username |
| `DB_PASSWORD` | `password` | Database password |
| `JWT_SECRET` | *(built-in default)* | Base64-encoded HMAC-SHA256 secret |
| `JWT_EXPIRATION` | `86400000` | Token TTL in milliseconds (default: 24 hours) |

Example (Linux / macOS):

```bash
export DB_URL="jdbc:mysql://localhost:3306/hr_portal?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true"
export DB_USERNAME="root"
export DB_PASSWORD="yourpassword"
export JWT_SECRET="yourBase64EncodedSecretKey"
export JWT_EXPIRATION="86400000"
```

> **Note:** The database `hr_portal` is created automatically if it does not exist (controlled by the `createDatabaseIfNotExist=true` parameter in the URL).

### Build and Run

**Run with Maven:**

```bash
mvn spring-boot:run
```

**Build an executable JAR and run it:**

```bash
mvn clean package
java -jar target/hr-portal-1.0.0.jar
```

The server starts on **http://localhost:8080** by default.

---

## API Endpoints

All protected endpoints require the `Authorization: Bearer <token>` header.

### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | ❌ | Register a new employee account |
| `POST` | `/api/auth/login` | ❌ | Log in and receive a JWT |

**Register — Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@example.com",
  "password": "Secret@123"
}
```

**Login — Request Body:**
```json
{
  "email": "jane.doe@example.com",
  "password": "Secret@123"
}
```

**Login — Response:**
```json
{
  "token": "<jwt>",
  "tokenType": "Bearer",
  "email": "jane.doe@example.com",
  "role": "EMPLOYEE"
}
```

---

### Employees

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `GET` | `/api/employees` | ✅ | `ADMIN` | Get all employees |
| `GET` | `/api/employees/me` | ✅ | Any | Get the authenticated employee's profile |
| `GET` | `/api/employees/{id}` | ✅ | Any* | Get employee by ID *(EMPLOYEE can only view their own)* |
| `PUT` | `/api/employees/{id}` | ✅ | `ADMIN` | Update an employee |
| `DELETE` | `/api/employees/{id}` | ✅ | `ADMIN` | Delete an employee |

---

### Departments

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `GET` | `/api/departments` | ✅ | Any | List all departments |
| `GET` | `/api/departments/{id}` | ✅ | Any | Get department by ID |
| `POST` | `/api/departments` | ✅ | `ADMIN` | Create a department |
| `PUT` | `/api/departments/{id}` | ✅ | `ADMIN` | Update a department |
| `DELETE` | `/api/departments/{id}` | ✅ | `ADMIN` | Delete a department |
| `PUT` | `/api/departments/{id}/assign/{employeeId}` | ✅ | `ADMIN` | Assign an employee to a department |

**Create Department — Request Body:**
```json
{
  "name": "Engineering",
  "description": "Software development team"
}
```

---

### Leave Requests

| Method | Endpoint | Auth | Role | Description |
|---|---|---|---|---|
| `POST` | `/api/leaves` | ✅ | Any | Submit a leave request |
| `GET` | `/api/leaves` | ✅ | Any* | List leave requests *(ADMIN sees all; EMPLOYEE sees own)* |
| `GET` | `/api/leaves/{id}` | ✅ | Any* | Get leave request by ID *(EMPLOYEE can only view their own)* |
| `PUT` | `/api/leaves/{id}/approve` | ✅ | `ADMIN` | Approve a leave request |
| `PUT` | `/api/leaves/{id}/reject` | ✅ | `ADMIN` | Reject a leave request |

**Submit Leave — Request Body:**
```json
{
  "leaveType": "ANNUAL",
  "startDate": "2025-06-01",
  "endDate": "2025-06-05",
  "reason": "Family vacation"
}
```

**Supported Leave Types:** `ANNUAL`, `SICK`, `CASUAL`, `UNPAID`

**Leave Statuses:** `PENDING`, `APPROVED`, `REJECTED`

---

## Authentication Flow

```
Client                          Server
  │                               │
  │  POST /api/auth/login         │
  │ ─────────────────────────────►│
  │  { email, password }          │
  │                               │  Validate credentials
  │                               │  Generate JWT (HMAC-SHA256)
  │  200 OK { token, role, ... }  │
  │ ◄─────────────────────────────│
  │                               │
  │  GET /api/employees/me        │
  │  Authorization: Bearer <jwt>  │
  │ ─────────────────────────────►│
  │                               │  Extract & validate JWT
  │                               │  Load UserDetails from DB
  │                               │  Set SecurityContext
  │  200 OK { employee profile }  │
  │ ◄─────────────────────────────│
```

- Tokens are signed with **HMAC-SHA256** and expire after **24 hours** by default.
- Each request is stateless — no server-side sessions or cookies are used.
- CSRF protection is disabled (appropriate for stateless JWT APIs).

---

## Role-Based Access Control

| Role | Permissions |
|---|---|
| `EMPLOYEE` | View own profile, submit leave requests, view own leave history |
| `ADMIN` | All EMPLOYEE permissions + manage all employees, departments, and leave requests |

New accounts registered via `/api/auth/register` are assigned the `EMPLOYEE` role by default. To grant admin access, update the `role` column for the user directly in the database.

---

## Project Structure

```
src/
├── main/
│   ├── java/com/hrportal/
│   │   ├── HrPortalApplication.java       # Application entry point
│   │   ├── config/
│   │   │   └── SecurityConfig.java        # Spring Security & JWT filter chain
│   │   ├── controller/                    # REST controllers
│   │   │   ├── AuthController.java
│   │   │   ├── EmployeeController.java
│   │   │   ├── DepartmentController.java
│   │   │   └── LeaveRequestController.java
│   │   ├── dto/
│   │   │   ├── request/                   # Inbound request DTOs
│   │   │   └── response/                  # Outbound response DTOs
│   │   ├── entity/                        # JPA entities & enums
│   │   ├── exception/                     # Global exception handling
│   │   ├── repository/                    # Spring Data JPA repositories
│   │   ├── security/                      # JWT provider & authentication filter
│   │   └── service/                       # Business logic services
│   └── resources/
│       └── application.properties         # Application configuration
└── test/
    └── java/com/hrportal/
        └── HrPortalApplicationTests.java  # Spring Boot context test
```

---

## Running Tests

The test suite uses an **H2 in-memory database** so no external database is required.

```bash
mvn test
```