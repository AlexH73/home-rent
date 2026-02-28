# 🏠 HomeRent – Property Rental Management System

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.0%2B-blue?logo=springsecurity)](https://spring.io/projects/spring-security)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-3.0%2B-lightgrey?logo=spring)](https://spring.io/projects/spring-data-jpa)
[![Liquibase](https://img.shields.io/badge/Liquibase-4.0%2B-blue?logo=liquibase)](https://www.liquibase.org/)
[![H2 Database](https://img.shields.io/badge/H2%20Database-2.0%2B-blue)](https://www.h2database.com/)
[![Swagger UI](https://img.shields.io/badge/Swagger%20UI-springdoc-green?logo=swagger)](https://swagger.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](./src/main/resources/docks/CONTRIBUTING.md)
![Status](https://img.shields.io/badge/Status-🚧%20Under%20Active%20Development-important)

**HomeRent** is a backend service for managing residential property rentals (long-term and short-term).  
The system provides role-based access for **tenants**, **property owners**, **operators**, and **administrators**.

![HomeRent Swagger UI](/src/main/resources/docks/screenshots/swagger/swagger_1.png)
---

## 🚀 Quick Start (Docker, PostgreSQL + pgAdmin)

### Prerequisites
- Docker + Docker Compose

### 1) Configure [environment](/src/main/resources/docks/environment_variables_eng.md)
Create a `.env` file in the project root (next to `docker-compose.yml`).

If you have `.env.example`, use:
```bash
cp .env.example .env
```

> ⚠️ Important Docker Compose note  
> Docker Compose substitutes `${VAR}` only from your **shell environment** or the **root `.env` file**.  
> `env_file:` passes variables into the container, but does **not** affect YAML interpolation.

Example `.env` keys:
```dotenv
# ---------- App ----------
APP_BASE_URL=http://localhost:8080

# ---------- Postgres (DB init) ----------
POSTGRES_DB=homerent_db
POSTGRES_USER=homerent_app
POSTGRES_PASSWORD=change-me

# ---------- App DB (Spring datasource) ----------
DB_NAME=homerent_db
DB_USERNAME=homerent_app
DB_PASSWORD=change-me
DB_PORT=5432

# ---------- Email (app) ----------
EMAIL_FROM_USERNAME=your@gmail.com
EMAIL_PASSWORD=your-app-password

# ---------- pgAdmin ----------
PGADMIN_PORT=5050
PGADMIN_DEFAULT_EMAIL=admin@example.com
PGADMIN_DEFAULT_PASSWORD=admin
```

### 2) Run
```bash
docker compose up --build
```

### Access points
- **App**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **pgAdmin**: http://localhost:5050

### pgAdmin connection
Register a new server in pgAdmin:
- Host name/address: `db` (service name inside docker network)
- Port: `5432`
- Maintenance database: `${POSTGRES_DB}` (e.g. `homerent_db`)
- Username: `${POSTGRES_USER}` (e.g. `homerent_app`)
- Password: `${POSTGRES_PASSWORD}`

---

## ▶️ Run locally (without Docker)

### Prerequisites
- Java 21+
- Maven 3.8+

```bash
git clone https://github.com/AlexH73/home-rent.git
cd home-rent
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 🧪 Application Profiles

| Profile | Database | Use Case |
|---------|----------|----------|
| `dev`  | H2 (in-memory) | Local development |
| `test` | H2 (isolated) | Running tests without external DB |
| `prod` | PostgreSQL | Docker / production-like run |
| `tc`   | Testcontainers (PostgreSQL) | Integration tests with real PostgreSQL in a container |

---

## ✨ Features

### 👤 For Tenants (ROLE_TENANT)
- Register, login, and manage profile
- Browse available properties with filters
- Book properties for selected dates
- Upload signed rental contracts (PDF)
- Simulate rental payments (emulation only)
- Receive email confirmations and receipts
- Submit repair/issue reports with photos

### 🏡 For Property Owners (ROLE_OWNER)
- Manage personal property listings
- View and respond to booking requests
- Approve or reject tenant bookings
- Attach documents, photos, and schematics to properties

### 👨‍💼 For Operators (ROLE_OPERATOR)
- Track active rentals
- Verify tenant documents
- Monitor and update issue reports
- Oversee booking lifecycles

### 👑 For Administrators (ROLE_ADMIN)
- Full user management (CRUD)
- Assign/modify user roles
- Oversee all properties and bookings
- System-wide configuration and monitoring

---

## 🛠 Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 3.5.10 |
| **Build Tool** | Maven | 3.8+ |
| **Security** | Spring Security | 6.0+ |
| **Persistence** | Spring Data JPA | 3.0+ |
| **Database** | H2 (dev/test), PostgreSQL (prod) | 2.0+ / 16+ |
| **Migrations** | Liquibase | 4.0+ |
| **Validation** | Bean Validation | 3.0+ |
| **API Docs** | SpringDoc OpenAPI + Swagger UI | 2.x |
| **Email** | Spring Mail + Thymeleaf | 3.0+ |
| **Testing** | Spring Boot Test, JUnit 5, MockMvc | 3.5+ |
| **Utilities** | Lombok | 1.18+ |
| **Templates** | Thymeleaf | 3.1+ |

---

## 🏗 Architecture

For a detailed package structure and architecture overview see:
- `src/main/resources/`[ARCHITECTURE.md](src/main/resources/ARCHITECTURE.md)

---

## 📚 API Endpoints

All endpoints are documented via **Swagger UI**:
- http://localhost:8080/swagger-ui/index.html

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/public/info` | System information |
| `POST` | `/api/auth/register` | User registration |
| `POST` | `/api/auth/login` | User authentication |

### Tenant API (ROLE_TENANT)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/tenant/properties/available` | Browse available properties |
| `GET` | `/api/tenant/properties/{id}` | View property details |
| `POST` | `/api/tenant/bookings` | Create booking request |
| `GET` | `/api/tenant/bookings/my` | View my bookings |
| `POST` | `/api/tenant/bookings/{id}/upload-contract` | Upload rental contract |
| `POST` | `/api/tenant/issues` | Report an issue |

### Owner API (ROLE_OWNER)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/owner/properties` | Manage my properties |
| `POST` | `/api/owner/properties` | Add new property |
| `DELETE` | `/api/owner/properties/{id}` | Delete property |
| `GET` | `/api/owner/bookings/pending` | View pending bookings |
| `POST` | `/api/owner/bookings/{id}/approve` | Approve booking |
| `POST` | `/api/owner/bookings/{id}/reject` | Reject booking |

### Operator API (ROLE_OPERATOR)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/operator/bookings/active` | View active bookings |
| `POST` | `/api/operator/bookings/{id}/activate` | Activate an APPROVED booking after verification |
| `GET` | `/api/operator/issues` | View all issue reports |
| `POST` | `/api/operator/issues/{id}/status` | Update issue status (`status=OPEN|IN_PROGRESS|DONE`) |

### Admin API (ROLE_ADMIN)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/admin/users` | Manage all users |
| `POST` | `/api/admin/users` | Create new user |
| `POST` | `/api/admin/users/{id}/roles` | Modify user roles |
| `GET` | `/api/admin/properties` | Manage all properties |
| `POST` | `/api/admin/properties` | Create property |
| `DELETE` | `/api/admin/properties/{id}` | Delete property |

---

## 🗄 Database & Migrations

- Database schema is managed via **Liquibase**.
- Changelog master: `classpath:db/changelog/db.changelog-master.xml`
- Docker/`prod` profile runs migrations automatically on startup.

### Liquibase Migration Structure
```
src/main/resources/db/changelog/
├── db.changelog-master.xml
├── 1.0-create-tables.xml
├── 1.1-insert-reference-data.xml
└── 1.2-test-objects.xml
```

> Note: Postgres container applies `POSTGRES_*` variables only on the **first initialization** (empty volume).
> If you change DB/user/password and want a clean init:
> ```bash
> docker compose down -v
> docker compose up --build
> ```

---

## 📧 Email Notifications

Email templates (Thymeleaf) are located in `src/main/resources/templates/mail/`.

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=BookingServiceTest

# Run integration tests only
mvn test -Dtest="*IT"
```

---

## 🤝 Contributing

We welcome contributions! See: `./src/main/resources/docks/CONTRIBUTING.md`

---

## 📄 License
MIT License. See [LICENSE](LICENSE).

---

## 📞 Support & Resources
- **GitHub Issues**: https://github.com/AlexH73/home-rent/issues
- **API Docs**: http://localhost:8080/swagger-ui/index.html