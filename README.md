# NSU Forums Backend

A Spring Boot REST API backend for the NSU web forum.

## Getting Started

### 1. Start infrastructure

```bash
docker compose up -d
```

Then, navigate to the RustFs console via, `http://localhost:9001/`,
login with the credentials found in application-local.yml,
and create a bucket with:

1. Name set to exactly `profile-icons`
2. Access policy set to `public`

### 2. Set up application profiles

Configure spring to use the application-local profile to load local dev configs.

## Api Documentation

Once running, Swagger UI is available at: `http://localhost:8080/swagger-ui/index.html#/`

## Testing

### Running Tests

```bash
# Unit tests only
./mvnw test

# Integration tests
docker compose up -d
./mvnw verify
```
