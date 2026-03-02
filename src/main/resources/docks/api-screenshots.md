# Документация API – Скриншоты Swagger и Postman

## Swagger UI
![Swagger UI](./screenshots/swagger/swagger_01.png)

### Общий вид
![Swagger Overview](./screenshots/swagger/swagger-overview.png)

*На скриншоте показаны все группы эндпоинтов: public, auth, tenant, owner, operator, admin.*

### Tenant API
![Swagger Tenant](./screenshots/swagger/swagger-tenant.png)

*Пример развёрнутого эндпоинта для создания бронирования. Видно описание, параметры и модель запроса.*

### Owner API
![Swagger Owner](./screenshots/swagger/swagger-owner.png)

*Эндпоинты управления объектами недвижимости и подтверждения бронирований.*

### Operator API
![Swagger Operator](./screenshots/swagger/swagger-operator.png)

*Просмотр активных аренд и заявок на поломки.*

### Admin API
![Swagger Admin](./screenshots/swagger/swagger-admin-users.png)
![Swagger Admin](./screenshots/swagger/swagger-admin-properties.png)

*Управление пользователями и всеми объектами.*

## Postman коллекция

### Структура коллекции
![Postman Collection](./screenshots/postman/postman-collection-overview.png)

*Коллекция разбита на папки по ролям: Auth, Public, Tenant, Owner, Operator, Admin.*

[Download Postman Collection](./attachments/HomeRent_API_postman_collection.json)

### Пример запроса регистрации
![Postman Register](./screenshots/postman/postman-register.png)

*Запрос POST /api/auth/register с телом и полученным ответом.*

### Пример запроса авторизации
![Postman Login](./screenshots/postman/postman-login.png)

*Запрос POST /api/auth/login с телом и полученным ответом.*

### Получение списка available недвижимостей по параметрам (Tenant)
![Postman Available Properties](./screenshots/postman/postman-available-properties.png)

*GET /api/tenant/properties/available с параметрами и ответом с результатами поиска.*

### Получение деталей недвижимости (Tenant)
![Postman Property Details](./screenshots/postman/postman-property-details.png)

*GET /api/tenant/properties/{id} с ответом, включающим все детали объекта недвижимости.*

### Создание бронирования (Tenant)
![Postman Create Booking](./screenshots/postman/postman-create-booking.png)

*Заголовок, тело запроса и успешный ответ.*

### Просмотр своих бронирований (Tenant)
![Postman My Bookings](./screenshots/postman/postman-my-bookings.png)

*GET /api/tenant/bookings/my с ответом, показывающим все бронирования текущего арендатора.*

### Загрузка договора (Tenant)
![Postman Upload Contract](./screenshots/postman/postman-upload-contract.png)

*POST /api/tenant/{id}/upload-contract Multipart-запрос с файлом PDF и ответом.*

### Подача заявки на поломку (Tenant)
![Postman Create Issue Request](./screenshots/postman/postman-create-issue.png)

*POST /api/tenant/issues с описанием проблемы и ответом.*

### Подтверждение бронирования (Owner)
![Postman Approve Booking](./screenshots/postman/postman-approve-booking.png)

*Владелец подтверждает бронирование через POST /api/owner/bookings/{id}/approve.*

### Просмотр активных аренд (Operator)
![Postman Active Bookings](./screenshots/postman/postman-operator-active.png)

*GET /api/operator/bookings/active с ответом.*

### Управление пользователями (Admin)
![Postman Admin Users](./screenshots/postman/postman-admin-users.png)

*GET /api/admin/users и пример ответа.*
