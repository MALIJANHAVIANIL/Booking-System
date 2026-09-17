# RESTful Resource Booking System

A clean, beginner-friendly RESTful Resource Booking System built using **Spring Boot 3**, **Spring Data JPA**, **Spring Security**, **JWT**, **PostgreSQL**, and **Swagger/OpenAPI**.

---

## 📌 Features

- **JWT Authentication & Stateless Security**: Secure login returning JWT Bearer token.
- **Role-Based Access Control (RBAC)**:
  - `ADMIN`: Full CRUD management of resources, view/update/delete all user reservations.
  - `USER`: Read-only access to resources, create/view/update/delete only their own reservations.
- **Strict Reservation Ownership Protection**:
  - User identity extracted automatically from `SecurityContextHolder` (JWT). `userId` is never trusted from request body.
- **Validation**:
  - Clean error handling with `@Valid`, `@DecimalMin`, `@NotBlank`, and start/end time ordering checks (`endTime > startTime`).
- **Dynamic Filtering, Pagination & Sorting**:
  - Dynamic database queries with Spring Data JPA `Specification`.
  - Configurable `status`, `minPrice`, `maxPrice`, `page`, `size`, and `sort` parameters (e.g. `sort=price,desc`).
- **Interactive OpenAPI Documentation**:
  - Swagger UI integrated with JWT Bearer Token authorization header support.

---

## 🛠️ Technology Stack

- **Java 17+**
- **Spring Boot 3.3.3**
- **Spring Web**
- **Spring Data JPA**
- **Spring Security**
- **JWT (io.jsonwebtoken 0.12.6)**
- **BCrypt Password Encoder**
- **PostgreSQL / H2 (for tests)**
- **Lombok**
- **Spring Boot Validation**
- **Maven**
- **Swagger / OpenAPI 3**
- **JUnit 5 & Mockito**

---

## 📋 Prerequisites

- **Java 17** or higher (`java -version`)
- **Maven 3.8+** (`mvn -version`)
- **PostgreSQL 14+** (running instance on `localhost:5432`)

---

## 🗄️ Database Setup & Environment Variables

Create a PostgreSQL database named `booking_db`:

```sql
CREATE DATABASE booking_db;
```

The system uses environment variables with fallback defaults:

| Environment Variable | Description | Default Value |
| :--- | :--- | :--- |
| `DB_URL` | PostgreSQL JDBC Connection URL | `jdbc:postgresql://localhost:5432/booking_db` |
| `DB_USERNAME` | PostgreSQL Username | `postgres` |
| `DB_PASSWORD` | PostgreSQL Password | `postgres` |
| `JWT_SECRET` | 256-bit Secret Key for signing JWTs | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` |

---

## 🔑 Seed Credentials

On application startup, `DataInitializer` automatically populates default test accounts and sample resources if the database is empty:

### ADMIN User
- **Email**: `admin@example.com`
- **Password**: `Admin@123`
- **Role**: `ADMIN`

### Standard USER
- **Email**: `user@example.com`
- **Password**: `User@123`
- **Role**: `USER`

---

## 🚀 How to Run the Application

### 1. Compile & Build

```bash
mvn clean install
```

### 2. Run Application

```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8081`.

---

## 🧪 How to Run Tests

Run the full suite of unit and integration tests (uses isolated in-memory H2 database):

```bash
mvn test
```

---

## 📖 Swagger / OpenAPI Documentation

Access Swagger UI interactive API documentation at:

```text
http://localhost:8081/swagger-ui.html
```

To test protected endpoints in Swagger UI:
1. Execute `POST /auth/login` with user or admin credentials.
2. Copy the returned `token`.
3. Click the **Authorize** button at the top right of Swagger UI.
4. Paste the token into the **Value** field and click **Authorize**.

---

## 🌐 API Endpoints Summary

### Authentication (Public)
- `POST /auth/login` - Authenticate user & get JWT token

### Resource Management (`/api/resources`)
- `POST /api/resources` - (ADMIN only) Create new resource
- `GET /api/resources` - (ADMIN, USER) Get list of all resources
- `GET /api/resources/{id}` - (ADMIN, USER) Get resource details by ID
- `PUT /api/resources/{id}` - (ADMIN only) Update resource details
- `DELETE /api/resources/{id}` - (ADMIN only) Delete resource

### Reservation Management (`/api/reservations`)
- `POST /api/reservations` - (USER, ADMIN) Create reservation (user identity loaded from JWT)
- `GET /api/reservations` - (USER sees own, ADMIN sees all) Get reservations with filtering, pagination, sorting
- `GET /api/reservations/{id}` - (USER sees own, ADMIN sees all) Get reservation details
- `PUT /api/reservations/{id}` - (USER updates own, ADMIN updates any) Update reservation status
- `DELETE /api/reservations/{id}` - (USER deletes own, ADMIN deletes any) Delete/cancel reservation

---

## 📬 Testing with Postman

A pre-configured Postman Collection file has been generated for you:
`Resource_Booking_System.postman_collection.json`

### How to use in Postman:
1. Open **Postman**.
2. Click **Import** (top left) and select [Resource_Booking_System.postman_collection.json](file:///c:/Users/Lenovo/Desktop/Test_17_9_2026/Resource_Booking_System.postman_collection.json).
3. Execute **Login - Standard User** or **Login - Admin**.
4. Copy the returned `token`.
5. Under the collection or request **Authorization** tab, select **Bearer Token** and paste the token.
6. Execute any request (`GET /api/resources`, `POST /api/reservations`, etc.).

---

## 💡 Example cURL Requests

### 1. Login to Get Token
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "User@123"
  }'
```

### 2. Create Reservation (User Token)
```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -d '{
    "resourceId": 1,
    "startTime": "2026-09-20T10:00:00",
    "endTime": "2026-09-20T12:00:00"
  }'
```

### 3. Get Reservations with Filtering, Pagination & Sorting
```bash
curl -X GET "http://localhost:8080/api/reservations?status=PENDING&minPrice=10.00&maxPrice=500.00&page=0&size=10&sort=price,desc" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```
