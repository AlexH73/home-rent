# 🏠 HomeRent – Property Rental Management System

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.0%2B-blue?logo=springsecurity)](https://spring.io/projects/spring-security)
[![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-3.0%2B-lightgrey?logo=spring)](https://spring.io/projects/spring-data-jpa)
[![Liquibase](https://img.shields.io/badge/Liquibase-4.0%2B-blue?logo=liquibase)](https://www.liquibase.org/)
[![H2 Database](https://img.shields.io/badge/H2%20Database-2.0%2B-blue)](https://www.h2database.com/)
[![Swagger UI](https://img.shields.io/badge/Swagger%20UI-2.5.0-green?logo=swagger)](https://swagger.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](./src/main/java/de/ait/homerent/main/resources/CONTRIBUTING.md)
![Status](https://img.shields.io/badge/Status-🚧%20Under%20Active%20Development-important)

**HomeRent** is a comprehensive backend service for managing residential property rentals, supporting both long-term and short-term leases. The system provides role-based access for tenants, property owners, operators, and administrators, following a clean, modular architecture.

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
| **Database** | H2 (dev/test) | 2.0+ |
| **Migrations** | Liquibase | 4.0+ |
| **Validation** | Bean Validation | 3.0+ |
| **API Docs** | SpringDoc OpenAPI + Swagger UI | 2.5.0 |
| **Email** | Spring Mail + Thymeleaf | 3.0+ |
| **Testing** | Spring Boot Test, JUnit 5, MockMvc | 3.5+ |
| **Utilities** | Lombok | 1.18+ |
| **Templates** | Thymeleaf | 3.1+ |

---

## 🏗 Architecture Overview

HomeRent follows a **domain-driven, role-based controller architecture** with strict separation of concerns according to the approved architecture:

### 🧱 Project Roots
```
src/main/java/de/ait/homerent
src/test/java/de/ait/homerent
```

### 🏠 Core Package Structure
```
homerent/
│
├── logs/
│   └── homerent.log
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── de/ait/homerent
│   │   │       │
│   │   │       ├── HomeRentApplication.java
│   │   │       │
│   │   │       ├── config/
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── OpenApiConfig.java
│   │   │       │   ├── MailConfig.java
│   │   │       │   └── StorageConfig.java
│   │   │       │
│   │   │       ├── security/
│   │   │       │   ├── CustomUserDetailsService.java
│   │   │       │   └── UserPrincipal.java
│   │   │       │
│   │   │       ├── auth/
│   │   │       │   ├── controller/
│   │   │       │   │   └── AuthController.java          // public endpoints
│   │   │       │   ├── dto/
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── RegisterRequest.java
│   │   │       │   │   └── AuthResponse.java
│   │   │       │   └── service/
│   │   │       │       └── AuthService.java
│   │   │       │
│   │   │       ├── user/
│   │   │       │   ├── model/
│   │   │       │   │   ├── User.java
│   │   │       │   │   └── Role.java
│   │   │       │   ├── repository/
│   │   │       │   │   ├── UserRepository.java
│   │   │       │   │   └── RoleRepository.java
│   │   │       │   ├── service/
│   │   │       │   │   └── UserService.java
│   │   │       │   └── controller/
│   │   │       │       └── AdminUserController.java     // ROLE_ADMIN only
│   │   │       │
│   │   │       ├── property/
│   │   │       │   ├── model/
│   │   │       │   │   ├── Property.java
│   │   │       │   │   └── PropertyStatus.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── PropertyRepository.java
│   │   │       │   ├── service/
│   │   │       │   │   └── PropertyService.java
│   │   │       │   └── controller/
│   │   │       │       ├── TenantPropertyController.java // ROLE_TENANT
│   │   │       │       ├── OwnerPropertyController.java  // ROLE_OWNER
│   │   │       │       └── AdminPropertyController.java  // ROLE_ADMIN
│   │   │       │
│   │   │       ├── booking/
│   │   │       │   ├── model/
│   │   │       │   │   ├── Booking.java
│   │   │       │   │   └── BookingStatus.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── BookingRepository.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── BookingCreateRequest.java
│   │   │       │   │   └── BookingResponse.java
│   │   │       │   ├── service/
│   │   │       │   │   └── BookingService.java
│   │   │       │   └── controller/
│   │   │       │       ├── TenantBookingController.java   // ROLE_TENANT
│   │   │       │       ├── OwnerBookingController.java    // ROLE_OWNER
│   │   │       │       └── OperatorBookingController.java // ROLE_OPERATOR
│   │   │       │
│   │   │       ├── contract/
│   │   │       │   ├── model/
│   │   │       │   │   └── RentalContract.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── RentalContractRepository.java
│   │   │       │   └── service/
│   │   │       │       └── RentalContractService.java
│   │   │       │
│   │   │       ├── issue/
│   │   │       │   ├── model/
│   │   │       │   │   ├── IssueReport.java
│   │   │       │   │   └── IssueStatus.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── IssueReportRepository.java
│   │   │       │   ├── service/
│   │   │       │   │   └── IssueService.java
│   │   │       │   └── controller/
│   │   │       │       ├── TenantIssueController.java     // ROLE_TENANT
│   │   │       │       └── OperatorIssueController.java   // ROLE_OPERATOR
│   │   │       │
│   │   │       ├── mail/
│   │   │       │   └── EmailService.java
│   │   │       │
│   │   │       ├── storage/
│   │   │       │   └── FileStorageService.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── NotFoundException.java
│   │   │       │   ├── AccessDeniedException.java
│   │   │       │   └── BadRequestException.java
│   │   │       │
│   │   │       └── util/
│   │   │           └── PriceCalculator.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-test.properties
│   │       ├── application-prod.properties
│   │       ├── logback-spring.xml
│   │       ├── .dockerignore
│   │       │
│   │       ├── db/changelog/
│   │       │   ├── db.changelog-master.xml
│   │       │   ├── 1.0-create-tables.xml
│   │       │   ├── 1.1-insert-reference-data.xml
│   │       │   └── 1.2-test-objects.xml
│   │       │
│   │       ├── templates/
│   │       │   └── mail/
│   │       │       ├── booking-confirmation.html
│   │       │       ├── contract-uploaded.html
│   │       │       └── rental-finished.html
│   │       │
│   │       ├── static/
│   │       │   └── swagger-ui.css
│   │       │
│   │       └── uploads/
│   │           ├── contracts/
│   │           └── issues/
│   │
│   └── test/
│       ├── java/
│       │   └── de/ait/homerent
│       │       ├── auth/
│       │       │   └── AuthControllerTest.java
│       │       │
│       │       ├── property/
│       │       │   ├── PropertyServiceTest.java
│       │       │   └── PropertyControllerTest.java
│       │       │
│       │       ├── booking/
│       │       │   ├── BookingServiceTest.java
│       │       │   ├── BookingControllerTest.java
│       │       │   └── BookingIntegrationTest.java
│       │       │
│       │       ├── issue/
│       │       │   └── IssueIntegrationTest.java
│       │       │
│       │       ├── security/
│       │       │   └── SecurityTest.java
│       │       │
│       │       └── integration/
│       │           ├── FullRentalFlowIT.java
│       │           └── IssueFlowIT.java
│       │
│       └── resources
│           ├── application-test.properties
│           └── db/changelog/
│               └── test-changelog.xml
│
├── pom.xml
├── .gitignore
├── Dockerfile
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.8+
- Git

### Quick Start
```bash
# Clone repository
git clone https://github.com/AlexH73/home-rent.git
cd home-rent

# Build project
mvn clean install

# Run with dev profile (H2 database)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Application Profiles
| Profile | Database | Use Case |
|---------|----------|----------|
| `dev` | H2 (in-memory) | Development with auto-reload |
| `test` | H2 (isolated) | Running tests |
| `prod` | PostgreSQL | Production deployment |

### Access Points
- **Application**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **H2 Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:homerentdb`)

---

## 📚 API Endpoints

All endpoints are documented via **Swagger UI** at `http://localhost:8080/swagger-ui.html`.

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
| `GET` | `/api/operator/bookings/active` | View active rentals |
| `GET` | `/api/operator/issues` | View all issue reports |
| `POST` | `/api/operator/issues/{id}/status` | Update issue status |

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

## 🗄 Database Configuration

### Development (H2)
```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:homerentdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

### Production (PostgreSQL)
```properties
# application-prod.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/homerent
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=validate
```

### Liquibase Migration Structure
```
src/main/resources/db/changelog/
├── db.changelog-master.xml
├── 1.0-create-tables.xml          # users, roles, properties, bookings, etc.
├── 1.1-insert-reference-data.xml  # roles: TENANT, OWNER, OPERATOR, ADMIN + test admin
└── 1.2-test-objects.xml          # test properties, bookings, issue reports
```

---

## 📧 Email Notifications

### Email Templates (Thymeleaf)
Located in `src/main/resources/templates/mail/`:
- `booking-confirmation.html` – Booking confirmation with dates and price
- `contract-uploaded.html` – Contract upload confirmation
- `rental-finished.html` – Rental completion receipt

### Email Scenarios
1. **Booking Confirmation** – Sent to tenant when booking is approved
2. **Contract Upload Notification** – Sent when contract is successfully uploaded
3. **Rental Completion Receipt** – Sent at end of rental period

---

## 🧪 Testing Strategy

### Test Types
| Type | Location | Tools | Purpose |
|------|----------|-------|---------|
| **Unit Tests** | `*ServiceTest.java` | JUnit 5, Mockito | Test business logic without Spring context |
| **Controller Tests** | `*ControllerTest.java` | MockMvc, @WebMvcTest | Test REST endpoints and role access |
| **Integration Tests** | `*IntegrationTest.java`, `*IT.java` | @SpringBootTest, H2, Liquibase | Test full scenarios with real database |

### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BookingServiceTest

# Run integration tests only
mvn test -Dtest="*IT"
```

### Test Scenarios
1. **Full Rental Flow**: Tenant books → Owner approves → Tenant uploads contract → Operator sees active rental
2. **Issue Reporting Flow**: Tenant reports issue → Operator updates status → Notifications sent

---

## 🤝 Contributing

We welcome contributions! Please follow our workflow:

### 1. Fork and Clone
```bash
git clone https://github.com/YOUR-USERNAME/home-rent.git
cd home-rent
```

### 2. Create Feature Branch
```bash
git checkout -b feature/amazing-feature
```

### 3. Commit Changes
```bash
git commit -m 'Add amazing feature'
```

### 4. Push and Create PR
```bash
git push origin feature/amazing-feature
```

### Development Guidelines
- Follow the established package structure
- Add tests for new functionality
- Update Swagger documentation for API changes
- Use meaningful commit messages
- Ensure code passes all tests before PR

### Code Style
- Java naming conventions
- Lombok for boilerplate reduction
- Spring Security annotations for access control
- Consistent formatting with 4-space indentation

---

## 👥 Contributors

| Contributor | Role | GitHub |
|-------------|------|--------|
| **AlexH73** | Lead Developer & Architect | [@AlexH73](https://github.com/AlexH73) |
| **dmitrined** | Backend Developer | [@dmitrined](https://github.com/dmitrined) |
| **TetianaAnufriieva** | Full Stack Developer | [@TetianaAnufriieva](https://github.com/TetianaAnufriieva) |
| **Gott-II** | DevOps & Security Specialist | [@Gott-II](https://github.com/Gott-II) |

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 📊 Project Status

**🚧 Under Active Development**

| Version | Status | Next Milestone |
|---------|--------|----------------|
| `0.0.1-SNAPSHOT` | In Development | Core MVP Features |
| `1.0.0` | Planned | Production Ready Release |

### Roadmap
- [ ] Payment gateway integration (stripe/paypal)
- [ ] Advanced search with geolocation
- [ ] Real-time notifications (WebSocket)
- [ ] Mobile app client
- [ ] Multi-language support
- [ ] Kubernetes deployment configuration

---

## 📞 Support & Resources

- **GitHub Issues**: [Report bugs or request features](https://github.com/AlexH73/home-rent/issues)
- **Project Wiki**: [Detailed documentation](https://github.com/AlexH73/home-rent/wiki)
- **API Documentation**: `http://localhost:8080/swagger-ui.html` (when running)

---

**Happy Renting! 🏠✨**

*This README is also available in: [Russian](README.ru.md) | [German](README.de.md)*