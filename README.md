# Backend

Spring Boot application.

## Requirements

- Java 21+
- Maven 3.6+

## Run

```bash
mvn spring-boot:run
```

The server starts at http://localhost:8080.

## Endpoints

- `GET /api/health` — Health check (returns `{"status":"UP"}`)

## Build

```bash
mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

## Tests

```bash
mvn test
```
