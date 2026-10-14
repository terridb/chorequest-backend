# ChoreQuest Backend

ChoreQuest is an app for sharing household chores. Members of a household earn points for the chores they finish and save them up together for a shared goal, like a movie night or a weekend trip.

This repository holds the REST API. The frontend lives in [chorequest-frontend](https://github.com/terridb/chorequest-frontend).

## Features

- **Households:** create a household, invite others with an invite code and manage members and roles.
- **Tasks:** chores that repeat on a schedule, from daily to yearly, or only when needed. A task is either always done by the same person or rotates between members.
- **Automatic planning:** a scheduler creates new assignments when a task is due again, based on the household's own time zone.
- **Points and goals:** each task has an effort level (5, 10, 15 or 30 points). Completing a task adds its points to the household's active goal.
- **Security:** authentication with Auth0 (JWT) and rate limiting per user on reads, writes and invites.

## Tech stack

- Java 25, Spring Boot 4
- PostgreSQL 18 with Flyway migrations
- Spring Security with Auth0 as OAuth2 resource server
- Bucket4j and Caffeine for rate limiting
- springdoc-openapi for API docs
- JUnit and Mockito for tests
- Docker, GitHub Actions and release-please

## Running locally

You need Java 25 and Docker.

1. Copy `.env.example` to `.env` and fill in the database name, user and password.

2. Start the database:

   ```bash
   docker compose up -d db
   ```

   The database is available on port `5433`.

3. Start the API with the `dev` profile. This profile loads seed data and turns on Swagger.

   ```bash
   DB_URL=jdbc:postgresql://localhost:5433/<db name> \
   DB_USERNAME=<user> \
   DB_PASSWORD=<password> \
   SPRING_PROFILES_ACTIVE=dev \
   ./mvnw spring-boot:run
   ```

The API runs on `http://localhost:8080`. In the `dev` profile the docs are at `http://localhost:8080/swagger-ui.html`.

The seed users have placeholder Auth0 IDs, so you can't log in as them. To get a filled household for your own account,
log in once on the local frontend, look up your Auth0 ID and run the demo script with it:

```bash
docker compose exec db psql -U <user> -d <db name> -c "select auth0_sub, created_at from users order by created_at desc"
docker compose exec -T db psql -U <user> -d <db name> -v sub='<auth0 id>' < scripts/demo-household.sql
```

This makes you the admin of a shared house with three housemates, open and completed tasks and an active goal. The
dates are relative to today, so running it again refreshes the household.

To run the database and the API together in Docker:

```bash
docker compose up -d
```

### Environment variables

| Variable                                                       | Description                                                    |
|----------------------------------------------------------------|----------------------------------------------------------------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`                         | PostgreSQL connection                                          |
| `SPRING_PROFILES_ACTIVE`                                       | Set to `dev` for seed data and Swagger                         |
| `CORS_ALLOWED_ORIGINS`                                         | Comma-separated frontend URLs, default `http://localhost:5173` |
| `AUTH0_ISSUER_URI`, `AUTH0_AUDIENCE`                           | Auth0 tenant and API identifier                                |
| `AUTH0_MANAGEMENT_CLIENT_ID`, `AUTH0_MANAGEMENT_CLIENT_SECRET` | Needed to delete accounts in Auth0                             |

## Tests

```bash
./mvnw test
```

## Project structure

```
src/main/java/com/terridb/chorequest
├── controllers    REST endpoints under /api
├── services       business logic
├── repositories   Spring Data JPA repositories
├── entities       JPA entities and enums
├── dtos           request and response objects
├── mappers        entity to DTO mapping
├── schedulers     job that creates due task assignments
├── filters        rate limiting
├── clients        Auth0 Management API client
├── exceptions     custom exceptions and the global error handler
├── validation     custom validation annotations
├── utils          small shared helpers
└── config         security, CORS and other configuration
```