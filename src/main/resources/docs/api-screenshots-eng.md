# API Documentation – Swagger and Postman Screenshots

## Swagger UI
![Swagger UI](./screenshots/swagger/swagger_01.png)

### Overview
![Swagger Overview](./screenshots/swagger/swagger-overview.png)

*The screenshot shows all endpoint groups: public, auth, tenant, owner, operator, admin.*

### Tenant API
![Swagger Tenant](./screenshots/swagger/swagger-tenant.png)

*Example of an expanded endpoint for creating a booking. Description, parameters, and request model are visible.*

### Owner API
![Swagger Owner](./screenshots/swagger/swagger-owner.png)

*Endpoints for managing properties and confirming bookings.*

### Operator API
![Swagger Operator](./screenshots/swagger/swagger-operator.png)

*View active rentals and issue requests.*

### Admin API
![Swagger Admin](./screenshots/swagger/swagger-admin-users.png)
![Swagger Admin](./screenshots/swagger/swagger-admin-properties.png)

*User management and management of all properties.*

## Postman Collection

### Collection Structure
![Postman Collection](./screenshots/postman/postman-collection-overview.png)

*The collection is organized into folders by role: Auth, Public, Tenant, Owner, Operator, Admin.*

[Download Postman Collection](./attachments/HomeRent_API_postman_collection.json)

### Registration Request Example
![Postman Register](./screenshots/postman/postman-register.png)

*POST request to /api/auth/register with body and received response.*

### Login Request Example
![Postman Login](./screenshots/postman/postman-login.png)

*POST request to /api/auth/login with body and received response.*

### Get list of available properties by parameters (Tenant)
![Postman Available Properties](./screenshots/postman/postman-available-properties.png)

*GET /api/tenant/properties/available with parameters and response containing search results.*

### Get property details (Tenant)
![Postman Property Details](./screenshots/postman/postman-property-details.png)

*GET /api/tenant/properties/{id} with response including all property details.*

### Create booking (Tenant)
![Postman Create Booking](./screenshots/postman/postman-create-booking.png)

*Headers, request body, and successful response.*

### View my bookings (Tenant)
![Postman My Bookings](./screenshots/postman/postman-my-bookings.png)

*GET /api/tenant/bookings/my with response showing all bookings of the current tenant.*

### Upload contract (Tenant)
![Postman Upload Contract](./screenshots/postman/postman-upload-contract.png)

*POST /api/tenant/{id}/upload-contract Multipart request with PDF file and response.*

### Submit issue request (Tenant)
![Postman Create Issue Request](./screenshots/postman/postman-create-issue.png)

*POST /api/tenant/issues with problem description and response.*

### Approve booking (Owner)
![Postman Approve Booking](./screenshots/postman/postman-approve-booking.png)

*Owner confirms booking via POST /api/owner/bookings/{id}/approve.*

### View active bookings (Operator)
![Postman Active Bookings](./screenshots/postman/postman-operator-active.png)

*GET /api/operator/bookings/active with response.*

### User management (Admin)
![Postman Admin Users](./screenshots/postman/postman-admin-users.png)

*GET /api/admin/users and example response.*