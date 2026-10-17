# Project structure

Where everything in the trainer service lives and why. It follows exactly the same layout as the
user service, so if you know one, you know the other.

## The short version

```
HTTP request
    |
    v
security/     Is there a valid JWT from the user service? Which user and role?
    |         (Public GET endpoints work without one.)
    v
controller/   Which URL is this? Is the JSON body valid?
    |
    v
service/      The business rules
    |
    v
repository/   Read or write the database
    |
    v
MySQL (tables created by db/migration/*.sql)
```

On the way back, `mapper/` turns entities into JSON-safe DTOs (`dto/response/`), and
`exception/` turns errors into the standard JSON error.

Rules:

1. **Each layer only talks to the one below it.** Controllers never touch repositories.
2. **Entities never leave the service layer.** The API returns DTOs only.
3. **This service has no users table.** It trusts the user service's JWT for who the user is, and
   stores only the user's id (`trainer_profiles.user_id`).

## Full tree

```
sportsaura_trainer_service/
├── .env.example                   Sample environment file; copy to .env
├── .github/workflows/ci.yml       CI: mvn verify on every push and pull request
├── .vscode/                       Lint lines and format-on-save settings, recommended extensions
├── config/checkstyle/checkstyle.xml   Lint rules (errors = red, warnings = yellow)
├── Dockerfile                     How to build the service image
├── docker-compose.yml             The service + its MySQL (port 3308)
├── docker-compose.tools.yml       phpMyAdmin for this database (port 8091), dev only
├── scripts/seed-dev-data.sh       Adds sample trainers through the APIs, dev only
├── pom.xml                        Dependencies and build plugins
├── README.md                      How to run and use the service
├── STRUCTURE.md                   This file
└── src/
    ├── main/java/com/sportsaura/trainerservice/
    │   ├── TrainerServiceApplication.java
    │   ├── config/
    │   ├── controller/
    │   ├── dto/request/
    │   ├── dto/response/
    │   ├── entity/
    │   ├── exception/
    │   ├── mapper/
    │   ├── repository/
    │   ├── security/
    │   └── service/
    ├── main/resources/
    │   ├── application.yml
    │   └── db/migration/          V1 ... V5
    └── test/
        ├── java/com/sportsaura/trainerservice/
        └── resources/application-test.yml
```

## Package by package

### `controller/` — the HTTP layer

| File | URLs |
|---|---|
| `SportController` | `GET /api/v1/sports`, `GET /api/v1/sports/{slug}` |
| `TrainerController` | `GET /api/v1/trainers`, `GET /api/v1/trainers/{id}`, `GET/PUT /api/v1/trainers/me` |
| `ContentController` | `GET /api/v1/content/{page}` |

`/me` endpoints carry `@PreAuthorize("hasRole('TRAINER')")`.

### `service/` — the business rules

| File | Responsibility |
|---|---|
| `SportService` | Lists active sports and adds how many trainers teach each one |
| `TrainerProfileService` | Search trainers; get one; create-or-update the signed-in trainer's own profile (checks every sport id exists) |
| `ContentService` | Loads all text blocks of a page and groups them by section |

### `repository/` — the database layer

| File | Responsibility |
|---|---|
| `SportRepository` | Active sports in display order, lookup by slug, trainer count per sport |
| `SportTrainerCount` | One row of that count query |
| `TrainerProfileRepository` | Lookup by user id, and `search(sport, city)` where a null filter means "any" |
| `ContentBlockRepository` | Active blocks of one page, sorted |

### `entity/` — database tables as Java classes

| File | Table |
|---|---|
| `Sport` | `sports` |
| `TrainerProfile` | `trainer_profiles`, plus its sports through `trainer_sports` |
| `ContentBlock` | `content_blocks` |

A trainer's sports are loaded in batches of 50 (`@BatchSize`), so a list page runs two queries,
not one per trainer.

### `dto/` — the shapes of the API

| Folder | Contains |
|---|---|
| `dto/request/` | `UpsertTrainerProfileRequest` with validation rules on each field |
| `dto/response/` | `SportResponse`, `SportSummaryResponse`, `TrainerResponse`, `ContentBlockResponse`, `PageContentResponse` |

### `mapper/`

`CatalogMapper` converts all three entities to their responses.

### `security/` — checking tokens from the user service

| File | Responsibility |
|---|---|
| `JwtService` | Checks a token's signature and expiry with the shared `JWT_SECRET`, reads user id, email and role. Never creates tokens |
| `JwtAuthenticationFilter` | On every request: a valid Bearer token marks the request as that user; none leaves it anonymous |
| `AuthenticatedUser` | The signed-in user as controllers see it (`id`, `email`, `role`) |
| `Role` | `USER`, `TRAINER`, `ADMIN` |
| `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler` | The JSON 401 and 403 responses |

### `config/`

| File | Responsibility |
|---|---|
| `SecurityConfig` | Public GET paths vs. paths that need a token |
| `JwtProperties` | Reads and validates `app.jwt.secret` |
| `PersistenceConfig` | Automatic `createdAt` / `updatedAt` |
| `OpenApiConfig` | Swagger UI title and the Authorize button |

### `exception/`

`ResourceNotFoundException` (404), `InvalidRequestException` (400) and `GlobalExceptionHandler`,
which formats every error the same way as the user service.

## Tests

| File | Kind |
|---|---|
| `CatalogIntegrationTest` | Whole app on real MySQL, every endpoint |
| `FlywayMigrationIntegrationTest` | Migrations, tables, reference data |
| `service/TrainerProfileServiceTest` | Unit |
| `security/JwtServiceTest` | Unit |
| `TestcontainersConfiguration` | Starts MySQL 8.4 in Docker |
| `TestTokens` | Builds tokens shaped like the user service's, for tests |

## Example: what happens on "show yoga trainers"

1. `GET /api/v1/trainers?sport=yoga` arrives. It is a public GET, so no token is needed.
2. `TrainerController.search` passes `sport=yoga` and paging to `TrainerProfileService.search`.
3. `TrainerProfileRepository.search` selects active trainers whose sports include `yoga`.
4. Their sports are loaded in one extra query, and `CatalogMapper` builds `TrainerResponse`s
   (no email or phone).
5. The controller returns them as a page: `{ content: [...], page: { totalElements, ... } }`.

## Where do I put new code?

| I want to... | Do this |
|---|---|
| Add a sport | New migration `V<n>__add_<sport>.sql` with an `INSERT` |
| Change page text locally | Edit `content_blocks` in phpMyAdmin |
| Ship new default page text | New migration with `INSERT`/`UPDATE` on `content_blocks` |
| Add a page section | Rows with a new `section_key`, then render it in the website |
| Add a field to trainer profiles | Migration → `TrainerProfile` → `UpsertTrainerProfileRequest` → `TrainerResponse` → `CatalogMapper` |
| Add an endpoint | Controller method → service method → DTOs |
| Make a URL public | Add it to `PUBLIC_READ_PATHS` in `SecurityConfig` |

Naming conventions are the same as the user service: lowercase packages, `<Thing>Controller`,
`<Thing>Service`, `<Entity>Repository`, `<Action>Request`, `<Thing>Response`, snake_case plural
tables, `/api/v1/<plural-noun>` URLs.
