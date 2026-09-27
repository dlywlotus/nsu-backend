# NSU Forums Backend

A Spring Boot REST API backend for the NSU web forum application.

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

### 2. Start the spring boot server

Create a run configuration for the app entrypoint with `Environment variables` set as
`--spring.profiles.active=local`
This starts the spring server with the properties defined in application-local.yml.

## Api Documentation

Once running, Swagger UI is available at: `http://localhost:8080/swagger-ui/index.html#/`

## Testing

### Running Tests

```bash
# Unit tests only
./mvnw test

# Unit and integration tests
./mvnw verify
```

## Architecture

### Tech stack

- Security: Spring Security
- Database: PostgreSQL
- Database abstraction layer: Spring Data Jpa
- Media Storage: RustFs (S3 compatible)
- Rate limiting: Bucket4J

### Authentication

The authentication system implementation involves OIDC and internal access and refresh
tokens. A security filter is set up to intercept requests and validate access tokens if they are
attached. The access tokens are short-lived while the refresh tokens last much longer.

## Deployment

1. Build the docker image using the following command:
   `mvn spring-boot:build-image -Dspring-boot.build-image.imageName=dlywlotus/nsu-backend:latest`
2. Push the image to docker hub using: `docker push dlywlotus/nsu-backend:latest`



