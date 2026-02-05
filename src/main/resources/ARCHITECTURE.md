# Архитектура проекта HomeRent

## 🧱 Корни проекта

```
src/main/java/de/ait/homerent
src/test/java/de/ait/homerent
```

---

## 🏠 src/main/java/de/ait/homerent

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
│   │   │       ├── config
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── OpenApiConfig.java
│   │   │       │   ├── MailConfig.java
│   │   │       │   └── StorageConfig.java
│   │   │       │
│   │   │       ├── security
│   │   │       │   ├── CustomUserDetailsService.java
│   │   │       │   └── UserPrincipal.java
│   │   │       │
│   │   │       ├── auth
│   │   │       │   ├── controller
│   │   │       │   │   └── AuthController.java          // public
│   │   │       │   ├── dto
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── RegisterRequest.java
│   │   │       │   │   └── AuthResponse.java
│   │   │       │   └── service
│   │   │       │       └── AuthService.java
│   │   │       │
│   │   │       ├── user
│   │   │       │   ├── model
│   │   │       │   │   ├── User.java
│   │   │       │   │   └── Role.java
│   │   │       │   ├── repository
│   │   │       │   │   ├── UserRepository.java
│   │   │       │   │   └── RoleRepository.java
│   │   │       │   ├── service
│   │   │       │   │   └── UserService.java
│   │   │       │   └── controller
│   │   │       │       └── AdminUserController.java     // ROLE_ADMIN
│   │   │       │
│   │   │       ├── property
│   │   │       │   ├── model
│   │   │       │   │   ├── Property.java
│   │   │       │   │   └── PropertyStatus.java
│   │   │       │   ├── repository
│   │   │       │   │   └── PropertyRepository.java
│   │   │       │   ├── service
│   │   │       │   │   └── PropertyService.java
│   │   │       │   └── controller
│   │   │       │       ├── TenantPropertyController.java // ROLE_TENANT
│   │   │       │       ├── OwnerPropertyController.java  // ROLE_OWNER
│   │   │       │       └── AdminPropertyController.java  // ROLE_ADMIN
│   │   │       │
│   │   │       ├── booking
│   │   │       │   ├── model
│   │   │       │   │   ├── Booking.java
│   │   │       │   │   └── BookingStatus.java
│   │   │       │   ├── repository
│   │   │       │   │   └── BookingRepository.java
│   │   │       │   ├── dto
│   │   │       │   │   ├── BookingCreateRequest.java
│   │   │       │   │   └── BookingResponse.java
│   │   │       │   ├── service
│   │   │       │   │   └── BookingService.java
│   │   │       │   └── controller
│   │   │       │       ├── TenantBookingController.java   // ROLE_TENANT
│   │   │       │       ├── OwnerBookingController.java    // ROLE_OWNER
│   │   │       │       └── OperatorBookingController.java // ROLE_OPERATOR
│   │   │       │
│   │   │       ├── contract
│   │   │       │   ├── model
│   │   │       │   │   └── RentalContract.java
│   │   │       │   ├── repository
│   │   │       │   │   └── RentalContractRepository.java
│   │   │       │   └── service
│   │   │       │       └── RentalContractService.java
│   │   │       │
│   │   │       ├── issue
│   │   │       │   ├── model
│   │   │       │   │   ├── IssueReport.java
│   │   │       │   │   └── IssueStatus.java
│   │   │       │   ├── repository
│   │   │       │   │   └── IssueReportRepository.java
│   │   │       │   ├── service
│   │   │       │   │   └── IssueService.java
│   │   │       │   └── controller
│   │   │       │       ├── TenantIssueController.java     // ROLE_TENANT
│   │   │       │       └── OperatorIssueController.java   // ROLE_OPERATOR
│   │   │       │
│   │   │       ├── mail
│   │   │       │   └── EmailService.java
│   │   │       │
│   │   │       ├── storage
│   │   │       │   └── FileStorageService.java
│   │   │       │
│   │   │       ├── exception
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── NotFoundException.java
│   │   │       │   ├── AccessDeniedException.java
│   │   │       │   └── BadRequestException.java
│   │   │       │
│   │   │       └── util
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
│   │       ├── db/changelog
│   │       │   ├── db.changelog-master.xml
│   │       │   ├── 1.0-create-tables.xml
│   │       │   ├── 1.1-insert-reference-data.xml
│   │       │   └── 1.2-test-objects.xml
│   │       │
│   │       ├── templates
│   │       │   └── mail
│   │       │       ├── booking-confirmation.html
│   │       │       ├── contract-uploaded.html
│   │       │       └── rental-finished.html
│   │       │
│   │       ├── static
│   │       │   └── swagger-ui.css
│   │       │
│   │       └── uploads
│   │           ├── contracts
│   │           └── issues
│   │
│   └── test/
│       ├── java/
│       │   └── de/ait/homerent
│       │       ├── auth
│       │       │   └── AuthControllerTest.java
│       │       │
│       │       ├── property
│       │       │   ├── PropertyServiceTest.java
│       │       │   └── PropertyControllerTest.java
│       │       │
│       │       ├── booking
│       │       │   ├── BookingServiceTest.java
│       │       │   ├── BookingControllerTest.java
│       │       │   └── BookingIntegrationTest.java
│       │       │
│       │       ├── issue
│       │       │   └── IssueIntegrationTest.java
│       │       │
│       │       ├── security
│       │       │   └── SecurityTest.java
│       │       │
│       │       └── integration
│       │           ├── FullRentalFlowIT.java
│       │           └── IssueFlowIT.java
│       │
│       └── resources
│           ├── application-test.properties
│           └── db/changelog
│               └── test-changelog.xml
│
├── pom.xml
├── .gitignore
├── Dockerfile
└── README.md

```

---

## 🧠 Как это читать (коротко)

- **main/java** — бизнес и инфраструктура

- **resources** — всё, что не Java

- **каждый домен** = свой пакет

- **каждая роль** = свой контроллер

- **test/java** зеркалит main/java

- **Liquibase и Email** — не размазаны, а на своих местах

---

## 👤 Распределение ролей по контроллерам

| Роль          | Контроллер                                                                   |
| ------------- | ---------------------------------------------------------------------------- |
| PUBLIC        | `AuthController`                                                             |
| ROLE_TENANT   | TenantPropertyController<br>TenantBookingController<br>TenantIssueController |
| ROLE_OWNER    | OwnerPropertyController<br>OwnerBookingController                            |
| ROLE_OPERATOR | OperatorBookingController<br>OperatorIssueController                         |
| ROLE_ADMIN    | AdminUserController<br>AdminPropertyController                               |

📌 **Контроллер = роль**
Никаких `if (role == ...)` внутри.

---

## 🔐 Security (коротко)

* `SecurityConfig`

    * public → `/api/auth/**`, `/api/public/**`
    * остальное → authenticated
* детальная проверка → `@PreAuthorize` **в service**

```java
@PreAuthorize("#booking.tenant.username == authentication.name")
```

---

## 🧪 Типы тестов

### ✅ Unit

* Service
* Mockito
* без контекста Spring

### ✅ Controller (WebMvcTest)

* MockMvc
* проверка ролей
* 401 / 403 / 200

### ✅ Integration

* `@SpringBootTest`
* H2 + Liquibase
* реальные сценарии:

    * полный цикл аренды
    * заявка → оператор → статус
