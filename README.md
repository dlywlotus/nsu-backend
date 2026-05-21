# NSU Forums Backend

A Spring Boot REST API backend for the NSU web forum.

## Getting Started

### 1. Start infrastructure

```bash
docker compose up -d
```

### 2. Set up application profies

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
