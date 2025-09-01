# GatewayApplication

A Spring Boot API Gateway for routing requests to microservices based on configuration. Supports **route exposure control** (public, private, protected) and dynamic URL routing.

---

## Features

- Dynamic routing based on service name in URL.
- Route exposure management:
    - `PUBLIC` – accessible by anyone.
    - `PRIVATE` – internal network access only.
    - `PROTECTED` – requires authentication.
- Forward HTTP requests (GET, POST, PUT, DELETE) to microservices.
- Configurable via `application.yml`.

---

## Prerequisites

- Java 17+
- Maven 3+
- Running microservices (e.g., `academic` on `localhost:8090`, `auth` on `localhost:5030`)
- Redis (optional, if using caching)

---

## Configuration

All services and routes are configured in `src/main/resources/application.yml`.

```yaml
spring:
  application:
    name: GatewayApplication
  redis:
    host: localhost
    port: 6379

server:
  port: 4000

services:
  list:
    - name: academic
      host: localhost
      port: 8090
      default-exposure: protected
      routes:
        - path: /api/institutes/:id
          method: GET
          exposure: public
        - path: /api/students
          method: POST
          exposure: protected
    - name: auth
      host: localhost
      port: 5030
      default-exposure: private

logging:
  level:
    root: INFO
