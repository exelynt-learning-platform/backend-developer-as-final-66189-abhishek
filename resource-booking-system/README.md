# Resource Booking System

A Spring Boot 3 RESTful booking API using Java 17, Spring Security, JWT, BCrypt, JPA/Hibernate, MySQL and Swagger/OpenAPI.

## 1. Prerequisites

Install:

- JDK 17 or newer
- Maven 3.9+
- MySQL 8+
- Eclipse or IntelliJ IDEA

Verify:

```bash
java -version
mvn -version
mysql --version
```

## 2. Create database

Open MySQL:

```sql
CREATE DATABASE resource_booking;
```

The application defaults to:

- DB URL: `jdbc:mysql://localhost:3306/resource_booking`
- username: `root`
- password: `root`

If your password is different, use environment variables.

## 3. Environment variables

Linux/macOS:

```bash
export DB_URL="jdbc:mysql://localhost:3306/resource_booking?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export DB_USERNAME="root"
export DB_PASSWORD="your_mysql_password"
export JWT_SECRET="replace-with-a-long-random-secret-at-least-32-characters"
```
For a quick local run, you can instead edit `src/main/resources/application.yml`.

## 4. Run from terminal

From the project root:

```bash
mvn clean install
mvn spring-boot:run
```

The application starts at:

`http://localhost:8080`

## 5. Run from IntelliJ IDEA

1. Open IntelliJ.
2. Select **Open** and choose the project folder.
3. Allow Maven to import dependencies.
4. Set Project SDK to Java 17+.
5. Make sure MySQL is running and the database exists.
6. Run `ResourceBookingApplication`.

## 6. Seed users

On first startup, these users are created:

### ADMIN

```text
username: admin
password: admin123
role: ADMIN
```

### USER

```text
username: user
password: user123
role: USER
```

Passwords are stored using BCrypt.

## 7. Swagger

Open:

`http://localhost:8080/swagger-ui.html`

OpenAPI JSON:

`http://localhost:8080/v3/api-docs`

## 8. Login

Request:

```http
POST /auth/login
Content-Type: application/json
```

```json
{
  "username": "user",
  "password": "user123"
}
```

The response contains a JWT.

For protected requests:

```http
Authorization: Bearer YOUR_TOKEN
```

## 9. Main APIs

### Authentication

```text
POST /auth/login
```

### Resources

```text
GET    /resources
GET    /resources/{id}
POST   /resources          ADMIN
PUT    /resources/{id}     ADMIN
DELETE /resources/{id}     ADMIN
```

### Reservations

```text
POST   /reservations
GET    /reservations
GET    /reservations/{id}
PUT    /reservations/{id}
DELETE /reservations/{id}
```

A USER only receives their own reservations. ADMIN receives all reservations.

## 10. Create reservation

Important: there is deliberately **no userId** in the request.

```json
{
  "resourceId": 1,
  "price": 250.00,
  "status": "PENDING",
  "startTime": "2026-09-10T10:00:00",
  "endTime": "2026-09-10T12:00:00"
}
```

The user is taken from the authenticated JWT/Spring Security context.

## 11. Reservation filtering

```text
GET /reservations?status=CONFIRMED
GET /reservations?minPrice=100
GET /reservations?maxPrice=500
GET /reservations?status=CONFIRMED&minPrice=100&maxPrice=500
```

## 12. Pagination

```text
GET /reservations?page=0&size=10
```

Maximum page size is 100.

## 13. Sorting

```text
GET /reservations?page=0&size=10&sort=price,desc
GET /reservations?page=0&size=10&sort=startTime,asc
```

Default:

```text
sort=id,desc
```

## 14. Database schema

Hibernate creates/updates these tables automatically for development:

```text
users
resources
reservations
```

Reservation relationships:

```text
reservations.user_id     -> users.id
reservations.resource_id -> resources.id
```

## 15. Build/test

```bash
mvn clean test
```

Package:

```bash
mvn clean package
```

Run the generated JAR:

```bash
java -jar target/resource-booking-system-1.0.0.jar
```


## 16. Automated tests

Run all tests:

```bash
mvn clean test
```

The test profile is:

```text
src/test/resources/application-test.yml
```

Production/development remains configured for MySQL in:

```text
src/main/resources/application.yml
```

## 17. GitHub submission

Initialize Git:

```bash
git init
git add .
git commit -m "Complete resource booking system"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/resource-booking-system.git
git push -u origin main
```
