# Task Management API

A modern REST API for managing projects, tasks, and team collaboration. Built with Spring Boot 4.0.6 and PostgreSQL, featuring JWT-based authentication and role-based access control.

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Runtime** | Java | 25 |
| **Framework** | Spring Boot | 4.0.6 |
| **ORM** | Spring Data JPA | Hibernate |
| **Database** | PostgreSQL | 12+ |
| **Security** | Spring Security + JWT | JJWT 0.12.6 |
| **Build Tool** | Gradle | 8.x |
| **Validation** | Spring Validation | Jakarta Bean Validation |
| **Utilities** | Lombok | Latest |

## System Requirements

- **Java 25** (OpenJDK or Oracle JDK)
- **PostgreSQL 12** or higher
- **Gradle 8.x** (or use provided Gradle Wrapper)
- **4GB RAM** minimum
- **500MB** disk space

## Project Setup

### 1. Prerequisites

Ensure you have the following installed:
```bash
java -version      # Should output Java 25
gradle -version    # Should output Gradle 8.x
psql -V           # Should output PostgreSQL 12+
```

### 2. Clone & Navigate

```bash
cd task-management-api
```

### 3. Database Configuration

Create a PostgreSQL database:
```sql
CREATE DATABASE taskdb;
CREATE USER admin WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE taskdb TO admin;
```

Or update connection settings in `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskdb
    username: admin
    password: password
```

### 4. Build the Project

```bash
./gradlew clean build
```

Or on Windows:
```bash
gradlew.bat clean build
```

### 5. Run the Application

```bash
./gradlew bootRun
```

The API will start on `http://localhost:8080`

## Project Structure

```
src/main/java/com/gideon/task_management_api/
├── TaskManagementApiApplication.java    # Main entry point
├── controller/                          # REST endpoints (TBD)
├── service/                             # Business logic
│   ├── UserService.java
│   └── ProjectService.java
├── entity/                              # JPA entities
│   ├── User.java
│   ├── Project.java
│   ├── Task.java
│   ├── ProjectMembership.java
│   ├── ProjectRole.java
│   └── TaskStatus.java
├── repository/                          # Data access
│   ├── UserRepository.java
│   ├── ProjectRepository.java
│   ├── TaskRepository.java
│   └── ProjectMembershipRepository.java
├── security/                            # Security config & JWT
│   └── SecurityConfig.java
└── dataTransfer/                        # DTOs (TBD)
```

## Core Entities

### User
- Unique username-based authentication
- Password hashing via Spring Security
- Tracks creation timestamp

### Project
- Owned by a single user
- Contains multiple tasks
- Unique name per owner
- Optional description

### Task
- Belongs to a project
- Can be assigned to users
- Status tracking (TODO, IN_PROGRESS, DONE)
- Auto-timestamped creation

### ProjectRole & ProjectMembership
- Role-based access control for projects
- Manage team members and their permissions

## Dependencies

Key dependencies included:
- `spring-boot-starter-webmvc` - Web framework
- `spring-boot-starter-data-jpa` - Database ORM
- `spring-boot-starter-security` - Authentication & authorization
- `spring-boot-starter-validation` - Input validation
- `postgresql` - Database driver
- `jjwt` - JWT token generation/validation
- `lombok` - Boilerplate reduction

## Testing

Run tests with:
```bash
./gradlew test
```

Test files are located in `src/test/java/` with comprehensive unit tests for services:
- `UserServiceTest.java` - User authentication & registration
- `ProjectServiceTest.java` - Project CRUD operations

## Database Schema

The application uses Hibernate with `ddl-auto: update` to automatically create/update tables on startup.

**Tables created:**
- `users` - User accounts
- `projects` - Project definitions
- `tasks` - Task items
- `project_memberships` - Team membership
- `project_roles` - Role definitions

## Environment Configuration

Default configuration in `application.yaml`:
```yaml
spring:
  application:
    name: task-management-api
  datasource:
    url: jdbc:postgresql://localhost:5432/taskdb
    username: admin
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## Development

### Compiling
```bash
./gradlew compileJava
```

### Building JAR
```bash
./gradlew build
```

### Running JAR
```bash
java -jar build/libs/task-management-api-0.0.1-SNAPSHOT.jar
```

## Current Status

✅ Entities & Database Models
✅ Security Configuration (Spring Security + JWT)
✅ Service Layer (UserService, ProjectService)
✅ Repository Layer
✅ Unit Tests

🚧 In Development:
- REST Controllers & Endpoints
- DTO Layer
- Additional validation rules
- API documentation (Swagger/OpenAPI)
- Advanced error handling

## Next Steps

1. Implement REST controllers for User and Project endpoints
2. Add DTO classes for request/response handling
3. Implement JWT token generation and validation
4. Add comprehensive error handling & logging
5. Add API documentation with Swagger
6. Implement integration tests
7. Add Docker support

## Support & Documentation

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Guide](https://spring.io/guides/gs/securing-web/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
