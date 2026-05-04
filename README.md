# NSU Forums Backend

A Spring Boot REST API backend for the NSU web forum, providing user authentication, posts with full-text search,
comments with one-level nesting, and a like system.

---

## Tech Stack

| Component        | Technology                          |
|------------------|-------------------------------------|
| Framework        | Spring Boot 4.0.1                   |
| Language         | Java 17                             |
| Database         | PostgreSQL 17                       |
| Cache / Sessions | Redis (Alpine)                      |
| ORM              | Spring Data JPA / Hibernate         |
| Auth             | JWT (jjwt 0.13.0) + Spring Security |
| Object Mapping   | MapStruct 1.7.0.Beta1               |
| Boilerplate      | Lombok                              |
| API Docs         | SpringDoc OpenAPI 2.8.5             |
| Build            | Maven + Spring Boot Maven Plugin    |
| Containers       | Docker Compose                      |

---

## Getting Started

### 1. Start infrastructure

```bash
docker compose up -d
```

This starts:

- PostgreSQL on port **5434** (db: `nsu`, user: `postgres`, password: `123`)
- Redis on port **6378** (password: `123`)

### 2. Initialize full-text search

Run the SQL in `src/main/resources/init.sql` against the database once after the `posts` table is created by Hibernate:

```bash
psql -h localhost -p 5434 -U postgres -d nsu -f src/main/resources/init.sql
```

This adds a `search_vector` column (generated tsvector) and a GIN index for full-text search on post title and body.

### 3. Configure environment

Copy `.env.example` to `.env` and fill in values, or use the `local` profile which has hardcoded defaults:

### 4. Build and run

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 5. Access API docs

Once running, Swagger UI is available at: `http://localhost:8080/swagger-ui/`

---

## Architecture Overview

The application follows a standard layered architecture:

```
HTTP Request
    │
    ▼
┌──────────────────┐
│  JwtFilter       │  ← Validates Bearer token, sets SecurityContext
└──────────────────┘
    │
    ▼
┌──────────────────┐
│  Controllers     │  ← Route handling, request validation (@Valid)
└──────────────────┘
    │
    ▼
┌──────────────────┐
│  Services        │  ← Business logic, authorization checks
└──────────────────┘
    │
    ▼
┌──────────────────┐
│  Repositories    │  ← Spring Data JPA + JdbcClient for raw SQL
└──────────────────┘
    │
    ▼
┌──────────────────┐
│  PostgreSQL      │  ← Persistent storage
│  Redis           │  ← Token state (refresh tokens, blacklist, versions)
└──────────────────┘
```

MapStruct mappers sit between controllers/services and entities, converting between DTOs and JPA entities.

---

## Database Schema

### users

| Column             | Type    | Constraints        |
|--------------------|---------|--------------------|
| id                 | UUID    | PK, auto-generated |
| username           | VARCHAR | UNIQUE             |
| encrypted_password | VARCHAR | NOT NULL           |
| profile_icon_url   | VARCHAR | nullable           |

### posts

| Column        | Type        | Constraints               |
|---------------|-------------|---------------------------|
| id            | UUID        | PK, auto-generated        |
| title         | VARCHAR     | NOT NULL                  |
| body          | VARCHAR     | NOT NULL                  |
| category      | VARCHAR     | NOT NULL (enum as string) |
| like_count    | INT         | NOT NULL, default 0       |
| created_at    | TIMESTAMPTZ | NOT NULL, audited         |
| author_id     | UUID        | FK → users(id), NOT NULL  |
| search_vector | TSVECTOR    | Generated stored column   |

### comments

| Column            | Type        | Constraints                   |
|-------------------|-------------|-------------------------------|
| id                | BIGINT      | PK, auto-generated (IDENTITY) |
| body              | VARCHAR     | NOT NULL                      |
| post_id           | UUID        | FK → posts(id), NOT NULL      |
| author_id         | UUID        | FK → users(id), NOT NULL      |
| parent_comment_id | BIGINT      | FK → comments(id), nullable   |
| created_at        | TIMESTAMPTZ | NOT NULL, audited             |

### likes

| Column  | Type   | Constraints                   |
|---------|--------|-------------------------------|
| id      | BIGINT | PK, auto-generated (IDENTITY) |
| user_id | UUID   | FK → users(id), NOT NULL      |
| post_id | UUID   | FK → posts(id), NOT NULL      |
|         |        | UNIQUE(user_id, post_id)      |

### Entity Relationships

```
User 1 ────── * Post
User 1 ────── * Comment
User 1 ────── * Like
Post 1 ────── * Comment
Post 1 ────── * Like
Comment 1 ─── * Comment  (self-referencing, max 1 level deep)
```

---

## API Reference

All endpoints except those listed under "Public" require a valid `Authorization: Bearer <access_token>` header.

### Authentication (Public)

| Method | Path             | Body            | Response                    | Notes                                    |
|--------|------------------|-----------------|-----------------------------|------------------------------------------|
| POST   | `/sign_up`       | `SignUpRequest` | `{"message": "..."}`        | Username 3-15 chars, password 6-15 chars |
| POST   | `/sign_in`       | `SignInRequest` | `UserAuthResponse` + cookie | Sets `refresh_token` HttpOnly cookie     |
| POST   | `/refresh_token` | —               | `UserAuthResponse` + cookie | Reads `refresh_token` cookie             |

### Authentication (Protected)

| Method | Path        | Body | Response             | Notes                                  |
|--------|-------------|------|----------------------|----------------------------------------|
| POST   | `/sign_out` | —    | `{"message": "..."}` | Requires Authorization header + cookie |

### Posts

| Method | Path                  | Body/Params                                           | Response               |
|--------|-----------------------|-------------------------------------------------------|------------------------|
| GET    | `/posts`              | `?category=&searchInput=&authorId=&page=&size=&sort=` | `List<PostDetails>`    |
| GET    | `/post/{postId}`      | —                                                     | `PostDetails`          |
| POST   | `/post`               | `AddPostRequest`                                      | `PostDetails`          |
| PUT    | `/post`               | `UpdatePostRequest`                                   | `PostDetails`          |
| DELETE | `/post/{postId}`      | —                                                     | `{"message": "..."}`   |
| GET    | `/post/{id}/comments` | —                                                     | `List<CommentDetails>` |

**Sorting**: `sort=created_at,desc` (default) or `sort=likes,desc`. Only the first sort parameter is used.

**Pagination**: `page=0&size=20` (defaults).

**Categories**: `EVENTS`, `STUDIES`, `HOUSING`, `OTHERS`

### Comments

| Method | Path            | Body                | Response             |
|--------|-----------------|---------------------|----------------------|
| POST   | `/comment`      | `AddCommentRequest` | `CommentDetails`     |
| DELETE | `/comment/{id}` | —                   | `{"message": "..."}` |

Comments support one level of nesting. Set `parentCommentId` to reply to a top-level comment. Replying to a nested
comment throws `NestedCommentException`.

### Likes

| Method | Path             | Response             |
|--------|------------------|----------------------|
| POST   | `/like/{postId}` | `{"message": "..."}` |
| DELETE | `/like/{postId}` | `{"message": "..."}` |

Like operations are transactional — the `like_count` on the post is updated atomically.

### User Profile

| Method | Path    | Body                   | Response      |
|--------|---------|------------------------|---------------|
| POST   | `/user` | `UpdateProfileRequest` | `UserDetails` |

---

## Authentication & Security

### Flow

1. **Sign up**: Creates user with BCrypt-hashed password (strength 10).
2. **Sign in**: Validates credentials, creates a JWT access token (15 min TTL) and a refresh token stored in Redis (30
   day TTL). The refresh token is returned as an HttpOnly, Secure, SameSite=Lax cookie.
3. **Authenticated requests**: The `JwtFilter` extracts the Bearer token, validates the signature, checks expiration,
   verifies the token version against Redis, and checks the blacklist.
4. **Token refresh**: The old refresh token is revoked, a new one is issued. If a revoked token is reused (stolen token
   scenario), all refresh tokens for that user are deleted and all access tokens are invalidated via version bump.
5. **Sign out**: The access token is blacklisted in Redis (15 min TTL matching token expiry), and the refresh token is
   revoked.

### Token Versioning

Each user has an access token version counter in Redis (`accessTokenVersionFor:{userId}`). When all tokens need to be
invalidated (e.g., stolen refresh token detected), the version is incremented. The `JwtFilter` rejects tokens with a
version lower than the current global version.

### Security Filter Chain

- CSRF is disabled (stateless JWT API).
- Public endpoints: `/sign_in`, `/sign_up`, `/refresh_token`, `/error`, `/api-docs/**`, `/swagger-ui/**`
- All other endpoints require authentication.
- `JwtFilter` runs before `AuthorizationFilter`.
- `CustomAuthenticationEntryPoint` returns a JSON 401 for unauthenticated requests.

---

## Redis Data Model

| Key Pattern                      | Type  | TTL     | Purpose                                    |
|----------------------------------|-------|---------|--------------------------------------------|
| `refreshToken:{uuid}`            | Hash  | 30 days | Fields: `isRevoked` (true/false), `userId` |
| `refreshTokensFor:{userId}`      | Set   | —       | All refresh token keys for a user          |
| `accessTokenVersionFor:{userId}` | Value | —       | Integer version counter                    |
| `blackListedAccessToken:{jwt}`   | Value | 15 min  | Blacklisted access token marker            |

---

## Full-Text Search

PostgreSQL full-text search is used for the `/posts` endpoint's `searchInput` parameter.

The `init.sql` script adds a generated `tsvector` column to the `posts` table:

- Title is weighted **A** (higher relevance)
- Body is weighted **B**
- A GIN index (`idx_search`) enables efficient lookups

Queries use `to_tsquery('english', :searchInput)` with the `@@` operator.

---

## Testing

### Running Tests

```bash
# Unit tests only
./mvnw test

# Integration tests (requires running PostgreSQL + Redis)
./mvnw verify
```

Integration tests use the `local` profile and require Docker services to be running. `AuthIT` flushes Redis before each
test. `PostIT` truncates the `posts` and `users` tables before each test.

The Maven Failsafe plugin is configured to pick up `*IT.java` files for integration tests.

### Test Infrastructure

- `@SpringBootTest(webEnvironment = RANDOM_PORT)` for integration tests
- `WebTestClient` (from spring-boot-starter-webflux) for HTTP assertions
- `JdbcClient` for direct SQL verification in `PostIT`
- Mockito + `@ExtendWith(MockitoExtension.class)` for unit tests
- MapStruct `Mappers.getMapper()` as `@Spy` in unit tests

---

## Configuration Reference

### application.yml (Production)

Uses environment variables for all sensitive values:

| Property                     | Env Variable        | Description                 |
|------------------------------|---------------------|-----------------------------|
| `spring.datasource.url`      | `POSTGRES_URL`      | JDBC connection string      |
| `spring.datasource.username` | `POSTGRES_USERNAME` | DB username                 |
| `spring.datasource.password` | `POSTGRES_PASSWORD` | DB password                 |
| `spring.data.redis.host`     | `REDIS_HOST`        | Redis hostname              |
| `spring.data.redis.port`     | `REDIS_PORT`        | Redis port                  |
| `spring.data.redis.password` | `REDIS_PASSWORD`    | Redis password              |
| `jwt.secret_key`             | `JWT_SECRET_KEY`    | Base64-encoded HMAC-SHA key |

### application-local.yml (Local Development)

Hardcoded values matching the Docker Compose setup:

- PostgreSQL: `localhost:5434`, user `postgres`, password `123`
- Redis: `localhost:6378`, password `123`
- JWT secret: Pre-configured Base64 key

### Hibernate DDL

`ddl-auto: update` — Hibernate auto-creates/updates tables on startup. The `search_vector` column and GIN index must
still be added manually via `init.sql`.

### Generating a JWT secret key

The secret key must be a Base64-encoded symmetric key suitable for HMAC-SHA. Refer to
the [jjwt documentation](https://github.com/jwtk/jjwt) for generation instructions.