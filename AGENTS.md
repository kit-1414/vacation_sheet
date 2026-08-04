# Vacation Sheet Development Guide

## Project Overview

Vacation Sheet is a local-first web application for vacation management. The repository is a monorepo with an Angular SPA, a Kotlin/Spring Boot backend, and PostgreSQL.

## Repository Structure

- `frontend/`: Angular SPA served by Nginx in Docker
- `backend/`: Kotlin and Spring Boot REST API
- `compose.yml`: complete local environment
- `README.md`: developer-facing setup instructions

## Technology Stack

Use the latest stable compatible versions unless a version is already pinned by the project.

### Frontend

- Angular 22
- TypeScript 6
- Angular Material 3
- Standalone components
- Angular Signals for state management
- Angular HttpClient
- SCSS
- Vitest-based Angular unit tests
- npm
- SPA only; do not add SSR

### Backend

- Java 21
- Kotlin 2.2
- Spring Boot 3.5
- Gradle Kotlin DSL
- Spring MVC REST API
- Spring Data JPA and Hibernate
- PostgreSQL 18
- Flyway migrations
- Spring Security OAuth2 Client
- Yandex OAuth2
- Springdoc Swagger UI
- Spring Boot Actuator
- JUnit 5, MockK, and Testcontainers

### Infrastructure

- Docker Compose starts PostgreSQL, backend, and frontend
- Nginx serves the Angular build and proxies backend requests
- The browser uses one origin for frontend and backend
- The application is exposed on port `4200`
- Backend and PostgreSQL are available only inside the Compose network

## Architecture Rules

- REST endpoints must use the `/api` prefix.
- Keep controllers thin and move business logic into services.
- Use request and response DTOs at the API boundary.
- Map DTOs manually; do not add MapStruct unless requirements change.
- Return API errors using Spring `ProblemDetail`.
- Validate incoming data with Jakarta Bean Validation.
- Keep changes small and avoid abstractions without a demonstrated need.
- Do not add OpenAPI contract files or generated TypeScript clients.
- Springdoc may generate the OpenAPI document used by Swagger UI.

## Persistence Rules

- Use Spring Data JPA repositories for persistence.
- Prefer derived repository methods and JPQL/HQL queries.
- Do not use the Criteria API.
- Do not use Hibernate schema generation for database changes.
- `spring.jpa.hibernate.ddl-auto` must remain `validate`.
- Every schema change must be implemented as a new Flyway migration.
- Never edit an applied migration; add the next versioned migration instead.
- Keep `spring.jpa.open-in-view` disabled.

## Security Model

- Authentication uses Yandex OAuth2 Authorization Code flow.
- Spring Boot is the OAuth2 client and stores authentication in an in-memory HTTP session.
- The Angular application must never receive Yandex access tokens.
- The session cookie must remain `HttpOnly` with `SameSite=Lax`.
- CSRF protection must remain enabled.
- Angular uses the `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` header.
- `/api/auth/csrf` initializes the CSRF token.
- Unauthenticated `/api/**` requests must return HTTP `401`, not redirect to login.
- OAuth login begins at `/oauth2/authorization/yandex`.
- Logout uses `POST /api/auth/logout`.
- Swagger endpoints are intentionally public:
  - `/swagger-ui.html`
  - `/swagger-ui/**`
  - `/v3/api-docs/**`
- `/actuator/health/**` is public for container health checks.

## Email Domain Access

The allowed Yandex email domain is configured with:

```yaml
app:
  security:
    allowed-email-domain: ""
```

- An empty value permits every email domain.
- A non-empty value requires an exact, case-insensitive domain match.
- Subdomains are not accepted unless explicitly configured.

## User Accounts And Roles

- Save a user in PostgreSQL on the first successful OAuth2 login.
- Update email, display name, and update timestamp on subsequent logins.
- The Yandex user ID is the external identity key.
- The application will have three roles, but their names and authorization model have not been decided.
- Do not invent or implement role semantics until requirements are agreed.

## Configuration

Main backend configuration is in `backend/src/main/resources/application.yml`.

Before testing real OAuth login, replace the placeholder Yandex `client-id` and `client-secret`. Do not commit real production secrets. The Yandex redirect URI for Docker is:

```text
http://localhost:4200/login/oauth2/code/yandex
```

The local database defaults are:

```text
Database: vacation_sheet
Username: vacation_sheet
Password: vacation_sheet
```

Compose overrides the JDBC URL so the backend connects to the `database` service.

## Existing API

- `GET /api/auth/csrf`: initializes and returns a CSRF token
- `GET /api/auth/me`: returns the authenticated persisted user
- `POST /api/auth/logout`: invalidates the current session
- `GET /actuator/health`: container health check
- `GET /v3/api-docs`: generated API description
- `GET /swagger-ui.html`: Swagger UI entry point

## Frontend Rules

- Use standalone Angular components.
- Use Signals and services for application state.
- Do not add NgRx unless state complexity demonstrates a concrete need.
- Use Angular Material components and preserve the existing visual language.
- Use the modern Angular template control flow (`@if`, `@for`).
- Use relative `/api` URLs; do not hard-code backend hosts in application code.
- Keep the UI responsive for desktop and mobile.
- Add unit tests for new stores, services, and non-trivial components.

## Backend Package Layout

The base package is `com.example.vacationsheet`.

- `config`: application and security configuration
- `security`: OAuth2 and access policies
- `user`: user persistence
- `web`: REST controllers, DTOs, and exception handling

Organize new business features by domain when they are introduced. Keep related controller, service, repository, entity, and DTO code close together instead of creating broad global layers.

## Development Commands

Run the complete environment from the repository root:

```shell
docker compose up --build
```

Stop containers without deleting PostgreSQL data:

```shell
docker compose down
```

Backend checks on Windows:

```shell
cd backend
gradlew.bat clean test bootJar
```

Backend checks on Unix-like systems:

```shell
cd backend
./gradlew clean test bootJar
```

Frontend checks:

```shell
cd frontend
npm ci
npm test -- --watch=false
npm run build
```

Validate Compose configuration:

```shell
docker compose config --quiet
```

## Definition Of Done

Before completing a change:

1. Add a Flyway migration for every database schema change.
2. Add or update focused tests for changed behavior.
3. Run backend tests and `bootJar` when backend code changes.
4. Run frontend unit tests and production build when frontend code changes.
5. Validate `docker compose config` when container configuration changes.
6. Verify protected endpoints still return `401` without a session.
7. Verify Swagger exclusions and CSRF behavior after security changes.
8. Do not commit OAuth secrets, generated build output, or dependency directories.
