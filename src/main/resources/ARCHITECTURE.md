# HomeRent — План проекта и Техническое задание

## 1. Обзор проекта
**HomeRent** — backend-сервис для автоматизации процессов долгосрочной и краткосрочной аренды жилья.

### Основные роли:
*   **TENANT** (Арендатор) — поиск, бронирование, подписание договора, заявки на ремонт.
*   **OWNER** (Владелец) — управление объектами, подтверждение бронирований.
*   **OPERATOR** (Оператор) — мониторинг активных аренд и обработка заявок на ремонт.
*   **ADMIN** (Администратор) — управление пользователями и системная модерация.

---

## 2. Технологический стек
*   **Java 17+**, **Spring Boot 3+**, **Maven**.
*   **Spring Data JPA**, **Hibernate**.
*   **Spring Security** (BCrypt, Role-based access, @EnableMethodSecurity).
*   **Liquibase** (миграции БД).
*   **Spring Mail + Thymeleaf** (Email-уведомления).
*   **Swagger (SpringDoc)** (документация API).
*   **Lombok**, **Validation API**.
*   **H2** (Dev/Test), **PostgreSQL/MySQL** (Prod - опционально).

---

## 3. Структура проекта (`de.ait.homerent`)

📁 java.de.ait.homerent

├── 📁 config
│       📄 SecurityConfig.java              # Настройка Spring Security, роли, BCrypt
│       📄 MailConfig.java                  # Конфигурация JavaMailSender
│       📄 OpenApiConfig.java               # Swagger/OpenAPI документация
│       📄 WebConfig.java                   # CORS, статические ресурсы

├── 📁 controllers
│       📄 AuthController.java              # POST /api/auth/register, POST /api/auth/login
│       📄 PropertyController.java          # GET /api/public/info
│       📄 TenantController.java            # API арендатора: /api/tenant/properties/available, /api/tenant/bookings, /api/tenant/issues
│       📄 OwnerController.java             # API владельца: /api/owner/properties, /api/owner/bookings/pending, /api/owner/bookings/{id}/approve
│       📄 OperatorController.java          # API оператора: /api/operator/bookings/active, /api/operator/issues
│       📄 AdminController.java             # API администратора: /api/admin/users, /api/admin/properties

├── 📁 dto
│       📄 UserDto.java / RegisterDto.java
│       📄 PropertyDto.java / NewPropertyDto.java
│       📄 BookingDto.java / BookingRequestDto.java
│       📄 IssueReportDto.java

├── 📁 enums
│       🔶 RoleName.java                    # TENANT, OWNER, OPERATOR, ADMIN
│       🔶 PropertyStatus.java              # AVAILABLE, BOOKED, RENTED, UNAVAILABLE
│       🔶 BookingStatus.java               # REQUESTED, APPROVED, REJECTED, ACTIVE, FINISHED
│       🔶 IssueStatus.java                 # OPEN, IN_PROGRESS, DONE

├── 📁 exceptions
│       📄 NotFoundException.java
│       📄 ForbiddenException.java
│       📄 BadRequestException.java
│       📄 GlobalExceptionHandler.java

├── 📁 model
│       📄 User.java                        # id, username, email, password, enabled, roles
│       📄 Role.java                        # id, name
│       📄 Property.java                    # id, owner, title, address, description, pricePerDay, status
│       📄 Booking.java                     # id, property, tenant, startDate, endDate, status, totalPrice
│       📄 RentalContract.java              # id, booking, filePath, uploadedAt
│       📄 IssueReport.java                 # id, booking, reportedBy, description, photoPath, status

├── 📁 repositories
│       📄 UserRepository.java
│       📄 PropertyRepository.java
│       📄 BookingRepository.java
│       📄 IssueReportRepository.java
│       📄 RentalContractRepository.java

├── 📁 services
│       📄 AuthService.java                 # Регистрация, логин
│       📄 PropertyService.java             # Поиск и фильтрация
│       📄 BookingService.java              # Проверка дат и расчет цены
│       📄 FileService.java                 # Сохранение файлов
│       📄 MailService.java                 # Отправка уведомлений

├── 📁 security
│       📄 CustomUserDetails.java           # UserDetails адаптер
│       📄 CustomUserDetailsService.java    # Загрузка пользователя из БД

├── 📁 validation
│       📄 DateRangeValidator.java          # Проверка диапазона дат

└── 📄 HomeRentApplication.java             # Главный класс приложения
│
├── 📁 resources
│       ├── 📁 db.changelog
│       │       📄 1.0-create-tables.xml
│       │       📄 1.1-insert-reference-data.xml
│       │       📄 1.2-test-objects.xml
│       │       📄 db.changelog-master.xml
│       │
│       ├── 📁 templates
│       │       📄 booking-confirmation.html
│       │       📄 contract-confirmation.html
│       │       📄 final-receipt.html
│       │
│       ├── 📄 application.properties
│       ├── 📄 application-dev.properties
│       ├── 📄 application-test.properties
│       └── 📄 application-prod.properties

---

## 4. Функциональные возможности

### 📄 Управление файлами
*   Загрузка PDF-договоров для бронирований.
*   Загрузка фото поломок (MultipartFile).
*   Хранение: файловая система (`/uploads/`) или БД.

### 📧 Email-уведомления
1.  **Подтверждение бронирования**: адрес, даты, цена.
2.  **Загрузка договора**: уведомление об успешной загрузке.
3.  **Завершение аренды**: квитанция с итоговой ценой.

### 🔐 Безопасность
*   `UserDetailsService` + `BCrypt`.
*   `@PreAuthorize` для проверки владельца объекта/бронирования.
*   Разделение доступа по префиксам эндпоинтов.

---

## 5. Тестирование
*   **Unit/Controller Tests**: проверка статус-кодов (200, 401, 403).
*   **Integration Tests**:
    *   Цикл аренды (создание -> подтверждение -> договор -> мониторинг).
    *   Цикл ремонта (создание заявки -> смена статуса оператором).

---

## 6. Замечания и предложения по улучшению
1.  **Автоматизация бизнес-логики**: смена статусов `PropertyStatus`, динамический расчет `totalPrice`.
2.  **Безопасность**: Stateless-авторизация (опционально JWT), Method Security.
3.  **Оптимизация**: Пагинация, Soft Delete.
4.  **Валидация**: Double Booking Prevention, информативные ошибки.
