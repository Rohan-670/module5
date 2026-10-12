# Spring Boot User Management REST API

A Java REST API implementing CRUD (Create, Read, Update, Delete) operations for users. It uses Spring Boot, Spring Web, Spring Data JPA, Bean Validation, and a database. A Postman collection is included with assertions for the CRUD flow and common error responses.

## Features

- `POST /api/users` — create a user (returns `201 Created` and a `Location` header)
- `GET /api/users` — list all users
- `GET /api/users/{id}` — fetch one user
- `PUT /api/users/{id}` — replace a user's editable fields
- `DELETE /api/users/{id}` — delete a user (returns `204 No Content`)
- Validates required names, email format, and `USER`/`ADMIN` role
- Normalizes email addresses to lowercase and prevents duplicate email addresses
- Consistent JSON errors for validation (`400`), missing users (`404`), and duplicate email (`409`)
- `createdAt` and `updatedAt` timestamps
- H2 database by default for easy local running; optional MySQL profile

> This is a user-record CRUD API, not a full authentication system. It does not implement sign-in, password storage, JWT, or authorization.

## Requirements

- JDK 17 or later
- Maven 3.6.3 or later
- Postman (to run the provided request collection)
- Optional: MySQL 8.x for the MySQL profile

## Run locally (H2 default)

1. Extract the ZIP and open a terminal in this project folder (where `pom.xml` is located).
2. Run:

   ```bash
   mvn clean test
   mvn spring-boot:run
   ```

3. Confirm the API is running at `http://localhost:8080`.
4. The default database is in-memory H2 and is reset when the application stops. H2 console is available at `http://localhost:8080/h2-console` while the application is running. JDBC URL: `jdbc:h2:mem:usersdb`, username: `sa`, password: *(blank)*.

## Use MySQL instead (optional)

1. Run MySQL and create the database/table using `schema-mysql.sql`. Create a database user with suitable privileges rather than using the root account for anything beyond local practice.
2. Configure environment variables. For Bash:

   ```bash
   export DB_URL='jdbc:mysql://localhost:3306/user_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
   export DB_USERNAME='your_mysql_user'
   export DB_PASSWORD='your_mysql_password'
   ```

   For PowerShell:

   ```powershell
   $env:DB_URL='jdbc:mysql://localhost:3306/user_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
   $env:DB_USERNAME='your_mysql_user'
   $env:DB_PASSWORD='your_mysql_password'
   ```

3. Start the app with the MySQL profile:

   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

   Or run a packaged jar with `java -jar target/user-management-api-1.0.0.jar --spring.profiles.active=mysql`.

## Test with Postman

1. Start the API first.
2. In Postman, click **Import** and import `postman/User-Management-API.postman_collection.json`.
3. Optionally import `postman/Local.postman_environment.json` and select the **User Management API - Local** environment.
4. Open the collection's **Run** / **Collection Runner**, keep the request order, and run the entire collection. The first request creates a unique email and stores the resulting ID for later requests.
5. The collection includes tests for creation, list, read, update, duplicate-email conflict, validation error, delete, and a 404 after deletion.

### Request examples

Create user — `POST http://localhost:8080/api/users`

```json
{
  "firstName": "Rohan",
  "lastName": "Gowda",
  "email": "rohan@example.com",
  "role": "USER"
}
```

Update user — `PUT http://localhost:8080/api/users/1`

```json
{
  "firstName": "Rohan",
  "lastName": "Gowda",
  "email": "rohan@example.com",
  "role": "ADMIN"
}
```

All requests that send JSON should include `Content-Type: application/json`.

## Project structure

```text
src/main/java/com/example/usermanagement/
  UserManagementApplication.java
  controller/UserController.java
  dto/UserRequest.java
  dto/UserResponse.java
  entity/User.java
  exception/ApiExceptionHandler.java
  exception/EmailAlreadyExistsException.java
  exception/UserNotFoundException.java
  repository/UserRepository.java
  service/UserService.java
src/main/resources/
  application.properties
  application-mysql.properties
postman/
  User-Management-API.postman_collection.json
  Local.postman_environment.json
schema-mysql.sql
pom.xml
```

## Notes

- The Postman collection contains executable assertions, but it must be run from Postman against a running instance; no Postman run or live MySQL connection is claimed by this project package.
- H2 is in-memory by default, so data is not preserved after shutdown. Choose the MySQL profile if records must remain after restarts.
- For production, add authentication and authorization, use a least-privilege database account, manage schema changes with migrations, and store secrets in a secure secret manager.
