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
![Swagger Owner](../screenshots/swagger/swagger-owner.png)
*Эндпоинты управления объектами недвижимости и подтверждения бронирований.*

### Operator API
![Swagger Operator](../screenshots/swagger/swagger-operator.png)
*Просмотр активных аренд и заявок на поломки.*

### Admin API
![Swagger Admin](../screenshots/swagger/swagger-admin.png)
*Управление пользователями и всеми объектами.*

## Postman коллекция

### Структура коллекции
![Postman Collection](../screenshots/postman/postman-collection-overview.png)
*Коллекция разбита на папки по ролям: Auth, Public, Tenant, Owner, Operator, Admin.*

[Download Postman Collection](./attachments/HomeRent_API_postman_collection.json)

### Пример запроса авторизации
![Postman Login](../screenshots/postman/postman-login.png)
*Запрос POST /api/auth/login с телом и полученным токеном.*

### Создание бронирования (Tenant)
![Postman Create Booking](../screenshots/postman/postman-create-booking.png)
*Заголовки с токеном, тело запроса и успешный ответ.*

### Подтверждение бронирования (Owner)
![Postman Approve Booking](../screenshots/postman/postman-approve-booking.png)
*Владелец подтверждает бронирование через POST /api/owner/bookings/{id}/approve.*

### Загрузка договора (Tenant)
![Postman Upload Contract](../screenshots/postman/postman-upload-contract.png)
*Multipart-запрос с файлом PDF.*

### Просмотр активных аренд (Operator)
![Postman Active Bookings](../screenshots/postman/postman-operator-active.png)
*GET /api/operator/bookings/active с ответом.*

### Управление пользователями (Admin)
![Postman Admin Users](../screenshots/postman/postman-admin-users.png)
*GET /api/admin/users и пример ответа.*
