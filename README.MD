# Task Management API

A REST API for managing projects, tasks, and team collaboration. Built with Spring Boot 4.0.6 and PostgreSQL, featuring JWT-based stateless authentication, role-based project membership, and containerized deployment via Docker Compose.

## Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Runtime** | Java | 25 |
| **Framework** | Spring Boot | 4.0.6 |
| **ORM** | Spring Data JPA | Hibernate 7.2 |
| **Database** | PostgreSQL | 17 |
| **Security** | Spring Security 7 + JWT | JJWT 0.12.6 |
| **Build Tool** | Gradle (Groovy) | 8.x |
| **Validation** | Jakarta Bean Validation | — |
| **Utilities** | Lombok | Latest |
| **Testing** | JUnit 5 + Mockito | — |
| **Containerization** | Docker + Docker Compose | Multi-stage build |

## Running the Project

### Option A — Docker Compose (recommended)

Spins up the API and PostgreSQL together, fully networked, no local Postgres install needed.

**Prerequisites:** Docker Desktop

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`. PostgreSQL is available at `localhost:5432` if you want to inspect it directly (e.g. via `psql` or a GUI client).

To stop:
```bash
docker compose down
```

Data persists across restarts via a named Docker volume. To wipe the database and start fresh:
```bash
docker compose down -v
```

### Option B — Local (no Docker for the app)

**Prerequisites:**
```bash
java -version      # Java 25
gradle -version    # Gradle 8.x
```

Requires a running PostgreSQL instance reachable at `localhost:5432` (a database `taskdb`, user `admin`, password `password` — or override via `application.yml`). The included Postgres Docker service can be used standalone for this:

```bash
docker compose up postgres
```

Then run the app directly:
```bash
./gradlew bootRun
```

On Windows:
```bash
gradlew.bat bootRun
```

The app resolves its database host via `${DB_HOST:localhost}` — no `DB_HOST` env var is set outside Docker, so it falls back to `localhost`, matching this setup automatically.

## Authentication

All endpoints except `/api/auth/**` require a JWT bearer token.

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"password\":\"password123\",\"name\":\"Test User\"}"
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"password\":\"password123\"}"
```

Both return an `AuthResponse` containing a JWT. Attach it to subsequent requests:
```bash
-H "Authorization: Bearer <token>"
```

Registration auto-generates a token (auto-login on register) — no separate login call needed to start using the API immediately after signing up.

## API Overview

### Auth (`/api/auth`) — public
| Method | Path | Description |
|--------|------|-------------|
| POST | `/register` | Create account, returns token |
| POST | `/login` | Authenticate, returns token |

### Projects (`/api/projects`) — authenticated
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create project |
| GET | `/{id}` | Get project by ID |
| PUT | `/{id}` | Update project |
| DELETE | `/{id}` | Delete project |
| POST | `/{id}/members` | Add member (defaults to `MEMBER` role) |
| DELETE | `/{id}/members/{userId}` | Remove member |
| GET | `/{id}/members` | List project members |

### Tasks
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/projects/{projectId}/tasks` | Create task in project |
| GET | `/api/projects/{projectId}/tasks` | List tasks in project |
| PUT | `/api/tasks/{taskId}/assign` | Self-assign task (`TODO` → `IN_PROGRESS`) |
| PUT | `/api/tasks/{taskId}/complete` | Mark complete (`IN_PROGRESS` → `DONE`, assignee only) |

Task status changes are action-based rather than data-based by design — there's no generic "update status" endpoint accepting an arbitrary status value. Assignment and completion are separate, constrained transitions.

## Project Structure

```
src/main/java/com/gideon/task_management_api/
├── TaskManagementApiApplication.java
├── controller/          # REST endpoints
│   ├── AuthController.java
│   ├── ProjectController.java
│   └── TaskController.java
├── service/              # Business logic, returns DTOs
│   ├── UserService.java
│   ├── ProjectService.java
│   └── TaskService.java
├── entity/                # JPA entities
│   ├── User.java
│   ├── Project.java
│   ├── Task.java (+ TaskStatus)
│   └── ProjectMembership.java (+ ProjectRole)
├── repository/             # Spring Data repositories
├── security/                # JWT filter, UserDetailsService, SecurityConfig
├── dataTransfer/             # Request/response DTOs (records)
└── exception/                  # Custom exceptions + GlobalExceptionHandler
```

## Error Handling

Centralized via `@RestControllerAdvice` (`GlobalExceptionHandler`), returning a consistent `ErrorResponse` shape:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "...",
  "timestamp": "2026-08-04T22:40:32"
}
```

Handled cases:
- Custom `ApiException` hierarchy — `ResourceNotFoundException` (404), `ForbiddenException` (403), `InvalidCredentialsException` (401), `DuplicateResourceException` (409), `InvalidStateException` (409)
- `DataIntegrityViolationException` — race-condition safety net (e.g. concurrent duplicate username), mapped to 409
- `MethodArgumentNotValidException` — Bean Validation failures, mapped to 400 with all field errors combined into one message

## Database Schema

Hibernate manages schema via `ddl-auto: update`.

**Tables:** `users`, `projects`, `tasks`, `project_memberships`

## Testing

```bash
./gradlew test
```

Unit tests cover the service layer (`UserServiceTest`, `ProjectServiceTest`) with JUnit 5 + Mockito.

Manual/live endpoint verification has been done via `curl` against the Dockerized stack — auth (register/login), project creation, and JWT-protected access have all been confirmed end-to-end.

## Environment Configuration

`src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: task-management-api
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/taskdb
    username: admin
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

jwt:
  secret: <base64-encoded secret>
  expiration: 86400000
```

`DB_HOST` defaults to `localhost` for local runs and is set to `task-management-db` (the Postgres service name) automatically by Docker Compose — see `docker-compose.yml`.

## Deployment

A multi-stage `Dockerfile` builds the JAR with `gradle:jdk25` and runs it on `eclipse-temurin:25-jre`. `docker-compose.yml` orchestrates the app and a PostgreSQL 17 container on a shared Docker-managed network, with the app resolving Postgres by container/service name via Docker's internal DNS rather than `localhost`.


## Documentation

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Guide](https://spring.io/guides/gs/securing-web/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Compose Reference](https://docs.docker.com/compose/)