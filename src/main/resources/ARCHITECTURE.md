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
HomeRent/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── de/
│   │   │       └── ait/
│   │   │           └── homerent/
│   │   │               ├── HomeRentApplication.java
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── AuthController.java
│   │   │               │   │   └── PublicController.java
│   │   │               │   ├── dto/
│   │   │               │   │   ├── AuthResponse.java
│   │   │               │   │   ├── LoginRequest.java
│   │   │               │   │   ├── RegisterRequest.java
│   │   │               │   │   └── RoleDto.java
│   │   │               │   └── service/
│   │   │               │       ├── AuthService.java
│   │   │               │       └── CustomUserDetailsService.java
│   │   │               ├── booking/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── OperatorBookingController.java
│   │   │               │   │   ├── OwnerBookingController.java
│   │   │               │   │   └── TenantBookingController.java
│   │   │               │   ├── dto/
│   │   │               │   │   ├── BookingCreateRequest.java
│   │   │               │   │   ├── BookingEmailRequest.java
│   │   │               │   │   ├── BookingResponse.java
│   │   │               │   │   └── RentalFinishedEmailRequest.java
│   │   │               │   ├── model/
│   │   │               │   │   ├── Booking.java
│   │   │               │   │   └── BookingStatus.java
│   │   │               │   ├── repository/
│   │   │               │   │   └── BookingRepository.java
│   │   │               │   ├── scheduler/
│   │   │               │   │   └── BookingFinishScheduler.java
│   │   │               │   └── service/
│   │   │               │       └── BookingService.java
│   │   │               ├── config/
│   │   │               │   ├── DevStartupLogger.java
│   │   │               │   ├── OpenApiConfig.java
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   ├── SwaggerCustomizer.java
│   │   │               │   └── WebConfig.java
│   │   │               ├── contract/
│   │   │               │   ├── dto/
│   │   │               │   │   └── ContractUploadedEmailRequest.java
│   │   │               │   ├── model/
│   │   │               │   │   └── RentalContract.java
│   │   │               │   ├── repository/
│   │   │               │   │   └── RentalContractRepository.java
│   │   │               │   └── service/
│   │   │               │       ├── FileStorageService.java
│   │   │               │       └── RentalContractService.java
│   │   │               ├── issue/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── OperatorIssueController.java
│   │   │               │   │   └── TenantIssueController.java
│   │   │               │   ├── dto/
│   │   │               │   │   ├── IssueCreateRequest.java
│   │   │               │   │   └── IssueReportResponse.java
│   │   │               │   ├── model/
│   │   │               │   │   ├── IssueReport.java
│   │   │               │   │   └── IssueStatus.java
│   │   │               │   ├── repository/
│   │   │               │   │   └── IssueReportRepository.java
│   │   │               │   └── service/
│   │   │               │       └── IssueService.java
│   │   │               ├── mail/
│   │   │               │   └── EmailService.java
│   │   │               ├── property/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── AdminPropertyController.java
│   │   │               │   │   ├── OwnerPropertyController.java
│   │   │               │   │   └── TenantPropertyController.java
│   │   │               │   ├── dto/
│   │   │               │   │   ├── PropertyCreateRequest.java
│   │   │               │   │   └── PropertyDto.java
│   │   │               │   ├── model/
│   │   │               │   │   ├── Property.java
│   │   │               │   │   ├── PropertyPhoto.java
│   │   │               │   │   └── PropertyStatus.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── PropertyPhotoRepository.java
│   │   │               │   │   └── PropertyRepository.java
│   │   │               │   └── service/
│   │   │               │       ├── PropertyFileStorageService.java
│   │   │               │       └── PropertyService.java
│   │   │               ├── security/
│   │   │               │   ├── BookingSecurity.java
│   │   │               │   └── PropertySecurity.java
│   │   │               ├── user/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AdminUserController.java
│   │   │               │   ├── dto/
│   │   │               │   │   ├── ErrorResponseDto.java
│   │   │               │   │   ├── UpdateRolesRequest.java
│   │   │               │   │   ├── UpdateUserStatusRequest.java
│   │   │               │   │   ├── UserCreateRequest.java
│   │   │               │   │   └── UserDto.java
│   │   │               │   ├── model/
│   │   │               │   │   ├── Role.java
│   │   │               │   │   ├── RoleName.java
│   │   │               │   │   └── User.java
│   │   │               │   ├── repository/
│   │   │               │   │   ├── RoleRepository.java
│   │   │               │   │   └── UserRepository.java
│   │   │               │   └── service/
│   │   │               │       └── UserService.java
│   │   │               └── utils/
│   │   │                   ├── CurrentUserHelper.java
│   │   │                   ├── FilePathUtils.java
│   │   │                   ├── LocalDateTimeDeserializer.java
│   │   │                   └── LocalDateTimeFormatter.java
│   │   └── resources/
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── application.properties
│   │       ├── ARCHITECTURE.md
│   │       ├── db/
│   │       │   └── changelog/
│   │       │       ├── 1.0-create-tables.xml
│   │       │       ├── 1.1-insert-reference-data.xml
│   │       │       ├── 1.2-test-objects.xml
│   │       │       └── db.changelog-master.xml
│   │       ├── docs/
│   │       │   ├── api-screenshots.md
│   │       │   ├── attachments/
│   │       │   │   └── HomeRent_API_postman_collection.json
│   │       │   ├── CONTRIBUTING.md
│   │       │   ├── environment_variables.md
│   │       │   ├── HomeRent.md
│   │       │   ├── HomeRent.pdf
│   │       │   └── screenshots/
│   │       │       ├── environment/
│   │       │       │   ├── Screenshot_007.png
│   │       │       │   ├── Screenshot_01.png
│   │       │       │   ├── Screenshot_02.png
│   │       │       │   ├── Screenshot_07.png
│   │       │       │   ├── Screenshot_08.png
│   │       │       │   ├── Screenshot_10.png
│   │       │       │   ├── Screenshot_2.png
│   │       │       │   ├── Screenshot_3.png
│   │       │       │   ├── Screenshot_4.png
│   │       │       │   ├── Screenshot_5.png
│   │       │       │   ├── Screenshot_6.png
│   │       │       │   ├── Screenshot_7.png
│   │       │       │   ├── Screenshot_8.png
│   │       │       │   └── Screenshot_9.png
│   │       │       └── postman/
│   │       │           ├── postman-admin-users.png
│   │       │           ├── postman-available-properties.png
│   │       │           ├── postman-collection-overview.png
│   │       │           ├── postman-create-booking.png
│   │       │           ├── postman-create-issue.png
│   │       │           ├── postman-login.png
│   │       │           ├── postman-my-bookings.png
│   │       │           ├── postman-operator-active.png
│   │       │           ├── postman-property-details.png
│   │       │           ├── postman-register.png
│   │       │           └── postman-upload-contract.png
│   │       ├── logback-spring.xml
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── swagger-custom.css
│   │       │   ├── images/
│   │       │   │   ├── home-background.png
│   │       │   │   ├── logo-swagger.png
│   │       │   │   └── logo.jpeg
│   │       │   └── js/
│   │       │       └── swagger-custom.js
│   │       └── templates/
│   │           └── mail/
│   │               ├── booking-confirmation.html
│   │               ├── contract-upload-confirmation.html
│   │               └── rental-finished-notice.html
│   └── test/
│       ├── java/
│       │   ├── de/
│       │   │   └── ait/
│       │   │       └── homerent/
│       │   │           ├── auth/
│       │   │           │   ├── controller/
│       │   │           │   │   ├── AuthControllerIT.java
│       │   │           │   │   ├── AuthControllerTest.java
│       │   │           │   │   ├── AuthControllerUTest.java
│       │   │           │   │   ├── PublicControllerIT.java
│       │   │           │   │   └── PublicControllerTest.java
│       │   │           │   └── service/
│       │   │           │       ├── AuthServiceTest.java
│       │   │           │       └── CustomUserDetailsServiceTest.java
│       │   │           ├── booking/
│       │   │           │   ├── BookingAvailabilityIT.java
│       │   │           │   ├── BookingFlowIT.java
│       │   │           │   ├── BookingOverlapIT.java
│       │   │           │   ├── controller/
│       │   │           │   │   └── OperatorBookingControllerTest.java
│       │   │           │   └── service/
│       │   │           │       └── BookingServiceTest.java
│       │   │           ├── contract/
│       │   │           │   └── service/
│       │   │           │       ├── FileStorageServiceTest.java
│       │   │           │       └── RentalContractServiceTest.java
│       │   │           ├── HomeRentApplicationTests.java
│       │   │           ├── issue/
│       │   │           │   ├── IssueFlowIT.java
│       │   │           │   ├── IssuePhotoUploadIT.java
│       │   │           │   ├── repository/
│       │   │           │   │   └── IssueReportRepositoryIT.java
│       │   │           │   └── service/
│       │   │           │       └── IssueServiceTest.java
│       │   │           ├── mail/
│       │   │           │   └── EmailServiceTest.java
│       │   │           ├── PostgresContainerIT.java
│       │   │           ├── property/
│       │   │           │   ├── PropertyAvailabilityIT.java
│       │   │           │   └── service/
│       │   │           │       ├── BookingServiceAdditionalTest.java
│       │   │           │       ├── PropertyFileStorageServiceTest.java
│       │   │           │       ├── PropertyServiceAdditionalTest.java
│       │   │           │       └── PropertyServiceTest.java
│       │   │           ├── security/
│       │   │           │   └── SecurityAccessIT.java
│       │   │           └── user/
│       │   │               ├── repository/
│       │   │               │   ├── RoleRepositoryIT.java
│       │   │               │   └── UserRepositoryIT.java
│       │   │               └── service/
│       │   │                   └── UserServiceTest.java
│       │   └── testsupport/
│       │       ├── it/
│       │       │   └── AbstractIT.java
│       │       └── security/
│       │           └── TestSecurityConfig.java
│       └── resources/
│           ├── application-tc.properties
│           └── application-test.properties
```

Notes:
- `.env` is expected locally for docker compose runs.
- `.env` is currently not present in the repository (recommended to add).
```