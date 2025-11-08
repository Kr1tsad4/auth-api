# Auth API

This project is an **Authentication API** supporting **Login, Register, and Logout** endpoints uses **JWT** for authentication and authorization.

---

## Requirements
- Java 17+
- Spring Boot 3.5.x
- MySQL 8+  
- Maven  

---

## Installation / Running the Project

1. Set environment variables in .env file:

2. Create the database and run the SQL script `create_users_table.sql`
   
3. Run the project:
   
   - **Using Maven to run the Spring Boot application:**
   
     ```bash
     mvn spring-boot:run
   
   - **Or build and run the jar manually:**
   
     ```bash
     # Build the jar file
     mvn clean package
   
     # Run the jar file
     java -jar target/auth-api-0.0.1-SNAPSHOT.jar

The application will run at `http://localhost:8080`

---


## API Endpoints

| Method | Endpoint                  | Description                                                   |
|--------|--------------------------|---------------------------------------------------------------|
| POST   | /auth/register            | Register a new user                                           |
| POST   | /auth/login               | Login                                                         |
| POST   | /auth/logout              | Logout                                                        |
| GET    | /users/{id}               | Get user details by ID (must match authenticated user ID in the token) |

## Request Body Examples

### Register 
```json
{
    "email": "test@gmail.com",
    "fullName": "John Doe",
    "password": "12345678",
    "dob": "2000-01-01",
    "phoneNumber": "0912345678" //Optional
}
```
### Login
```json
{
    "email": "test@gmail.com",
    "password": "12345678",
}
```
---


## Notes
- Endpoints that require authentication (Get user details, Logout) must include a **JWT token** in the `Authorization` header.
- Refresh tokens are stored in **HTTP-only cookies** and do not need to be sent manually.
- Users have `roles` and `status` (Active/Inactive) for access control.
