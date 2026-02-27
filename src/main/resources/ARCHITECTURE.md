# HomeRent — Architecture

This document describes the backend architecture of **HomeRent**: package boundaries, domain responsibilities, security model, persistence/migrations, and testing approach.

---

## 1) Project roots

```
src/main/java/de/ait/homerent
src/test/java/de/ait/homerent
src/main/resources
```

---

## 2) Architectural style (high level)

HomeRent follows a **domain-oriented, role-based REST architecture**:

- **Domain packages**: `auth`, `user`, `property`, `booking`, `contract`, `issue`, ...
- **Role-based controllers**: a separate controller per role (Tenant/Owner/Operator/Admin)
- **Service layer** holds business logic
- **Persistence layer** uses Spring Data JPA repositories
- **Database schema** is managed by Liquibase (no Hibernate auto-ddl)

Key principles:
- No "if role == ..." branching inside controllers/services — access is expressed via **Spring Security rules**.
- REST controllers are thin: validation + delegate to services.
- Migrations are deterministic and environment-aware using Liquibase contexts.

---

## 3) Package map (overview)

Main entry:
- `de.ait.homerent.HomeRentApplication`

Infrastructure/config:
- `de.ait.homerent.config.*` (Security, OpenAPI, Mail, Storage, Swagger UI customization)

Domains (examples):
- `auth` — login/register/public endpoints
- `user` — user + roles, admin user management
- `property` — property catalog and ownership management
- `booking` — booking lifecycle (tenant requests → owner approves/rejects → operator activates)
- `contract` — rental contract upload & storage
- `issue` — maintenance issues lifecycle
- `mail` — email notifications
- `storage` — file storage abstractions
- `exception` — global exception handling

---

## 4) Domain responsibilities & key endpoints

### 4.1 Auth
Responsibilities:
- registration / authentication
- public system endpoints

Typical endpoints:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/public/info`

### 4.2 Property
Responsibilities:
- property CRUD (owner/admin)
- property browsing (tenant)

### 4.3 Booking
Responsibilities:
- tenant creates booking request
- owner approves/rejects
- operator activates booking after verification

Operator workflow endpoints:
- `GET /api/operator/bookings/active`
- `POST /api/operator/bookings/{id}/activate`

### 4.4 Contract
Responsibilities:
- upload signed rental contracts (PDF)
- store and link uploaded file to a booking/rental flow

### 4.5 Issue
Responsibilities:
- tenant creates issue report
- operator updates issue status

Operator workflow endpoints:
- `GET /api/operator/issues`
- `POST /api/operator/issues/{id}/status?status=OPEN|IN_PROGRESS|DONE`

---

## 5) Security model

### 5.1 HTTP security (route-level)
Configured in `SecurityConfig`:
- Public: `/api/auth/**`, `/api/public/**`
- Swagger/OpenAPI endpoints are publicly accessible
- Role-based route prefixes:
  - `/api/tenant/**` → TENANT or ADMIN
  - `/api/owner/**` → OWNER or ADMIN
  - `/api/operator/**` → OPERATOR or ADMIN
  - `/api/admin/**` → ADMIN

### 5.2 Method security (fine-grained)
Enabled via `@EnableMethodSecurity`.

Used when access depends on ownership/business rules, e.g.:
- "Tenant can access only own bookings"
- "Owner can manage only own properties"
- "Operator actions require OPERATOR role"

(Prefer placing such rules at service layer, not controllers.)

---

## 6) Persistence & migrations

### 6.1 Persistence
- Spring Data JPA repositories per domain
- Hibernate DDL is not used for schema creation (`ddl-auto=validate`)

### 6.2 Liquibase
Liquibase changelog master:
- `classpath:db/changelog/db.changelog-master.xml`

Typical changelog structure:
- `1.0-create-tables.xml` — schema
- `1.1-insert-reference-data.xml` — roles, reference data
- `1.2-test-objects.xml` — demo/test objects (when enabled by context)

### 6.3 Environments / contexts
Liquibase contexts allow separating production-safe migrations from seeded demo/test data.

---

## 7) Static resources & Swagger UI customization

Swagger UI is provided via SpringDoc.
Custom styling/behavior:
- `src/main/resources/static/css/swagger-custom.css`
- `src/main/resources/static/js/swagger-custom.js`

Integration:
- `SwaggerCustomizer` injects CSS/JS into Swagger UI `index.html`.

---

## 8) Profiles

| Profile | Database | Purpose |
|--------|----------|---------|
| `dev` | H2 in-memory | Local development |
| `test` | H2 isolated | Unit/integration tests without external DB |
| `prod` | PostgreSQL | Docker/production-like run |
| `tc` | Testcontainers PostgreSQL | Integration tests with real PostgreSQL |

---

## 9) Testing strategy

### 9.1 Automated tests
- **Unit tests**: service layer, Mockito, no Spring context
- **WebMvc tests**: controller tests + security expectations (401/403/200)
- **Integration tests**: full Spring context, database migrations applied, scenario-based flows

### 9.2 Manual testing (Swagger UI / Postman)
Manual checks are used to validate the end-to-end flows in a running environment (usually Docker + `prod` profile).

Typical manual scenarios:
- **Auth**: register → login → use token/credentials
- **Property browsing & management**: create property (owner/admin) → list → update/delete
- **Booking flow**: tenant creates booking → owner approves/rejects → operator activates
- **Contract upload**: upload PDF contract for a booking
- **Issues flow**: tenant creates issue → operator updates status (OPEN → IN_PROGRESS → DONE)

Tools:
- Swagger UI: `/swagger-ui/index.html`
- Postman collections (optional / if provided in repository)

---

## 10) Directory tree (reference)

> Note: This is a reference tree for navigation. Some packages may evolve over time.

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
│   │   │       │   │   ├── AuthController.java
│   │   │       │   │   └── PublicController.java          // public endpoints
│   │   │       │   ├── dto/
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── RegisterRequest.java
│   │   │       │   │   └── AuthResponse.java
│   │   │       │   └── service/
│   │   │       │       ├── AuthService.java
│   │   │       │       └── CustomUserDetailsService.java
│   │   │       │
│   │   │       ├── user/
│   │   │       │   ├── model/
│   │   │       │   │   ├── RoleName.java
│   │   │       │   │   ├── User.java
│   │   │       │   │   └── Role.java
│   │   │       │   ├── repository/
│   │   │       │   │   ├── UserRepository.java
│   │   │       │   │   └── RoleRepository.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── UpdateRolesRequest.java
│   │   │       │   │   ├── UserCreateRequest.java
│   │   │       │   │   └── UserDto.java
│   │   │       │   ├── service/
│   │   │       │   │   └── UserService.java
│   │   │       │   └── controller/
│   │   │       │       └── AdminUserController.java     // ROLE_ADMIN only
│   │   │       │
│   │   │       ├── property/
│   │   │       │   ├── model/
│   │   │       │   │   ├── Property.java
│   │   │       │   │   ├── PropertyPhoto.java
│   │   │       │   │   └── PropertyStatus.java
│   │   │       │   ├── repository/
│   │   │       │   │   ├── PropertyPhotoRepository.java
│   │   │       │   │   └── PropertyRepository.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── PropertyCreateRequest.java
│   │   │       │   │   └── PropertyDto.java
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
│   │   │       │   │   ├── BookingEmailRequest.java
│   │   │       │   │   ├── BookingResponse.java
│   │   │       │   │   └── RentalFinishedEmailRequest.java
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
│   │   │       │   ├── dto/
│   │   │       │   │   └── ContractUploadedEmailRequest.java
│   │   │       │   └── service/
│   │   │       │       ├── FileStorageService.java
│   │   │       │       └── RentalContractService.java
│   │   │       │
│   │   │       ├── issue/
│   │   │       │   ├── model/
│   │   │       │   │   ├── IssueReport.java
│   │   │       │   │   └── IssueStatus.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── IssueReportRepository.java 
│   │   │       │   ├── dto/
│   │   │       │   │   ├── IssueCreateRequest.java
│   │   │       │   │   └── IssueReportResponse.java
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
│   │   │       └── utils/
│   │   │           └── FilePathUtils.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-test.properties
│   │       ├── application-prod.properties
│   │       ├── logback-spring.xml
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
├── .dockerignore
├── Dockerfile
└── README.md
```

Notes:
- `.env` is expected locally for docker compose runs.
- `.env` is currently not present in the repository (recommended to add).
```