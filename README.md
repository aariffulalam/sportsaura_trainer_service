# SportsAura Trainer Service

The service behind everything a visitor browses on SportsAura:

- **Sports catalog**: the sports trainers can teach (football, cricket, tennis, ...).
- **Trainer profiles**: headline, bio, city, experience, hourly rate and sports of each trainer.
- **Website content**: the text of the public pages (home, how it works, for trainers), stored in
  the database so it can change without a new release.

Reading is public, so people who are not signed in can browse. Trainers manage their own profile
with the login token (JWT) issued by the
[user service](https://github.com/aariffulalam/sportsaura_user_service).

- How the code is organised: [STRUCTURE.md](STRUCTURE.md)
- Interactive API docs once running: <http://localhost:8082/swagger-ui.html>

## Contents

1. [Tech stack](#tech-stack)
2. [What you need installed](#what-you-need-installed)
3. [Quick start with Docker](#quick-start-with-docker)
4. [Sample data](#sample-data)
5. [Run on your machine for development](#run-on-your-machine-for-development)
6. [Environment variables](#environment-variables)
7. [Using the API](#using-the-api)
8. [Editing website content](#editing-website-content)
9. [Code quality: lint and format](#code-quality-lint-and-format)
10. [Tests](#tests)
11. [Database migrations](#database-migrations)
12. [Useful commands](#useful-commands)
13. [Troubleshooting](#troubleshooting)

## Tech stack

Same stack as every SportsAura service: Java 25, Spring Boot 3.5 (Web, Data JPA, Security,
Validation, Actuator), MySQL 8.4, Flyway, JJWT (token checking only), springdoc (Swagger UI),
Maven wrapper, Spotless + google-java-format (formatter), Checkstyle (linter), Testcontainers,
Docker, GitHub Actions.

## What you need installed

| Tool | Needed for | Check |
|---|---|---|
| Docker Desktop | Running the service and database, and the integration tests | `docker --version` |
| JDK 25 | Running, testing and editing outside Docker | `java -version` |
| The user service | Getting login tokens (only needed to change trainer profiles) | <http://localhost:8081/actuator/health> |

## Quick start with Docker

```bash
cp .env.example .env            # Windows PowerShell: Copy-Item .env.example .env
# Edit .env: change the passwords, and set JWT_SECRET to the SAME value as the user service.
docker compose up --build
```

When you see `Started TrainerServiceApplication`:

- Health check: <http://localhost:8082/actuator/health>
- Swagger UI: <http://localhost:8082/swagger-ui.html>
- Sports: <http://localhost:8082/api/v1/sports>

With the database browser (phpMyAdmin on <http://localhost:8091>):

```bash
docker compose -f docker-compose.yml -f docker-compose.tools.yml up -d
```

## Sample data

| Data | Where it comes from | When |
|---|---|---|
| 8 sports | Migration `V4__add_sports_catalog.sql` | Automatically, on first start |
| Website text for home, how-it-works and for-trainers | Migration `V5__add_site_content.sql` | Automatically, on first start |
| 5 sample trainers | `scripts/seed-dev-data.sh` | Only when you run it (development only) |

Sports and website text are real data that every environment needs, so they are migrations.
Sample trainers are fake people, so they come from a script you run only on your machine:

```bash
# user service (8081) and trainer service (8082) must both be running
bash scripts/seed-dev-data.sh
```

It creates the trainer accounts in the user service (if missing), logs in as each one, and saves
their profile through this API. Running it again updates the profiles instead of duplicating them.
All sample accounts use the password `Sportsaura@123`.

| Trainer | Email | City | Sports |
|---|---|---|---|
| Rahul Sharma | rahul.trainer@sportsaura.dev | Mumbai | Football, Fitness |
| Priya Nair | priya.trainer@sportsaura.dev | Bengaluru | Yoga, Swimming |
| Vikram Singh | vikram.trainer@sportsaura.dev | Delhi | Cricket |
| Meera Iyer | meera.trainer@sportsaura.dev | Chennai | Tennis, Badminton |
| Arjun Patel | arjun.trainer@sportsaura.dev | Pune | Basketball, Fitness |

## Run on your machine for development

```bash
cp .env.example .env
docker compose up -d trainer-db      # MySQL on localhost:3308
./mvnw spring-boot:run               # Windows: mvnw.cmd spring-boot:run
```

## Environment variables

| Variable | Required | Default | Meaning |
|---|---|---|---|
| `SERVER_PORT` | no | `8082` | Port the service listens on |
| `DB_HOST` | no | `localhost` | MySQL host. Docker Compose sets `trainer-db` for the app container |
| `DB_PORT` | no | `3306` | MySQL port. The sample uses `3308`, where Docker publishes MySQL |
| `DB_NAME` | no | `sportsaura_trainer` | Database name |
| `DB_USERNAME` | **yes** | none | MySQL user the app logs in with |
| `DB_PASSWORD` | **yes** | none | Password of that user |
| `DB_ROOT_PASSWORD` | Docker only | none | MySQL root password, used by Docker Compose to create the database |
| `JWT_SECRET` | **yes** | none | **Must equal the user service's `JWT_SECRET`.** Used to check login tokens |
| `PHPMYADMIN_PORT` | no | `8091` | Port of phpMyAdmin (`docker-compose.tools.yml`) |

## Using the API

Base URL `http://localhost:8082`. Errors use the same JSON shape as the user service
(`status`, `detail`, and `errors` per field for validation).

| Method | URL | Who | What it does |
|---|---|---|---|
| GET | `/api/v1/sports` | anyone | All sports in display order, each with `trainerCount` |
| GET | `/api/v1/sports/{slug}` | anyone | One sport, e.g. `/api/v1/sports/yoga` |
| GET | `/api/v1/trainers` | anyone | Trainers. Filters: `?sport=yoga&city=Pune`. Paging: `?page=0&size=20` |
| GET | `/api/v1/trainers/{id}` | anyone | One trainer's public profile |
| GET | `/api/v1/content/{page}` | anyone | Website text for `home`, `how-it-works` or `for-trainers`, grouped by section |
| GET | `/api/v1/trainers/me` | TRAINER | My profile (404 until I create it) |
| PUT | `/api/v1/trainers/me` | TRAINER | Create or update my profile |

Public profiles never include email or phone; those stay in the user service.

### Create or update my trainer profile

```bash
# 1. Log in at the user service as a trainer
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"rahul.trainer@sportsaura.dev","password":"Sportsaura@123"}' \
  | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

# 2. Save the profile here (sport ids from GET /api/v1/sports)
curl -X PUT http://localhost:8082/api/v1/trainers/me \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"displayName":"Rahul Sharma","headline":"AIFF D-licensed football coach",
       "bio":"Eight years coaching in Mumbai.","city":"Mumbai",
       "experienceYears":8,"hourlyRateInr":900,"sportIds":[1,6]}'
```

Rules: 1 to 5 sports, hourly rate ₹100 to ₹1,00,000, experience 0 to 60 years. A `USER` or
`ADMIN` token gets `403`; no token gets `401`.

### Page content

```bash
curl http://localhost:8082/api/v1/content/home
```

```json
{
  "page": "home",
  "sections": {
    "hero":     [{ "title": "Find and hire the right sports trainer", "body": "...", "icon": null,
                   "linkLabel": "Browse trainers", "linkUrl": "/trainers" }],
    "features": [{ "title": "Trainers for every sport", "body": "...", "icon": "🏅", ... }],
    "cta":      [{ "title": "Are you a coach?", ... }]
  }
}
```

## Editing website content

Each piece of text is one row in the `content_blocks` table:

| Column | Meaning |
|---|---|
| `page_key` | Which page: `home`, `how-it-works`, `for-trainers` |
| `section_key` | Which part of the page, e.g. `hero`, `features`, `players`, `benefits`, `cta` |
| `sort_order` | Position inside the section (1, 2, 3, ...) |
| `title`, `body` | Heading and text |
| `icon` | Optional emoji |
| `link_label`, `link_url` | Optional button text and where it goes, e.g. `/register` |
| `active` | `0` hides the block without deleting it |

To change text locally, open phpMyAdmin (<http://localhost:8091>) → `content_blocks` → edit the
row. The website shows it on the next page load. Sections the website knows about:

| Page | Sections |
|---|---|
| `home` | `hero` (1 block), `features` (cards), `cta` (1 block) |
| `how-it-works` | `intro` (1 block), `players` (steps), `trainers` (steps) |
| `for-trainers` | `hero` (1 block), `benefits` (cards), `cta` (1 block) |

To ship new default text to every environment, add a migration (see below), never edit
`V5__add_site_content.sql`. An admin screen for this is planned.

## Code quality: lint and format

Same tools and rules as the user service: Spotless (formatter, like Prettier) and Checkstyle
(linter, like ESLint) with `config/checkstyle/checkstyle.xml`. Both run in `./mvnw verify` and CI.

```bash
./mvnw spotless:apply     # fix formatting
./mvnw verify             # compile + tests + format check + lint (run before every push)
```

Open the folder in VS Code with JDK 25 installed and accept the recommended extensions to see red
(error) and yellow (warning) lines while typing.

## Tests

```bash
./mvnw verify
```

**Docker must be running**: integration tests start a real MySQL 8.4 (Testcontainers), run every
migration and call the real endpoints with tokens signed like the user service's.

| Test | Covers |
|---|---|
| `CatalogIntegrationTest` | Public sports/trainers/content, filters, create and update a profile, 400/401/403/404 |
| `FlywayMigrationIntegrationTest` | All 5 migrations applied, tables, sample sports and content, emoji stored correctly |
| `TrainerProfileServiceTest` | Create vs update, unknown sports, blank filters |
| `JwtServiceTest` | Valid, expired, wrong-secret, role-less and garbage tokens |

No JDK installed? Run it in Docker:

```bash
docker run --rm -v "${PWD}:/app" -v sportsaura-m2:/root/.m2 \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal \
  -w /app maven:3.9-eclipse-temurin-25 mvn -B verify
```

## Database migrations

Flyway SQL files in `src/main/resources/db/migration/`, applied automatically on start.
Hibernate only validates (`ddl-auto: validate`).

| File | What it does |
|---|---|
| `V1__create_sports_table.sql` | `sports` table |
| `V2__create_trainer_profiles_tables.sql` | `trainer_profiles` and `trainer_sports` (which sports each trainer teaches) |
| `V3__create_content_blocks_table.sql` | `content_blocks` table |
| `V4__add_sports_catalog.sql` | The 8 sports |
| `V5__add_site_content.sql` | Text for the three public pages |

- Never edit a migration that has run. Add `V6__...sql`.
- `trainer_profiles.user_id` is the account id in the **user service**. There is no foreign key
  to it, because that table lives in another service's database.
- MySQL runs with `utf8mb4` so emoji icons are stored correctly.

## Useful commands

| Task | Command |
|---|---|
| Start everything | `docker compose up --build` |
| Start with phpMyAdmin | `docker compose -f docker-compose.yml -f docker-compose.tools.yml up -d` |
| Stop (keep data) | `docker compose -f docker-compose.yml -f docker-compose.tools.yml down` |
| Stop and delete the database | add `-v` to the line above |
| Add sample trainers | `bash scripts/seed-dev-data.sh` |
| App logs | `docker compose logs -f trainer-service` |
| Full check before pushing | `./mvnw verify` |

## Troubleshooting

| Problem | Cause and fix |
|---|---|
| Every call with a token returns 401 | `JWT_SECRET` differs from the user service's. Copy the exact value |
| `PUT /trainers/me` returns 403 | The account's role is not `TRAINER`. Register with role `TRAINER` |
| `Unknown sport id(s): [..]` | Use ids from `GET /api/v1/sports` |
| Emoji show as `?` | The database was created without `utf8mb4`. `docker compose down -v` and start again |
| `seed-dev-data.sh` prints `SKIP` | That email exists in the user service with a different password |
| Tests fail with `Could not find a valid Docker environment` | Start Docker Desktop |
| `Could not resolve placeholder 'JWT_SECRET'` | No `.env` file. Copy `.env.example` to `.env` |

---

**Author:** Aarif Alam
