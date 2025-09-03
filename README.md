# Gateway Application

This is a Spring Boot Gateway service that forwards incoming requests to microservices based on route configuration. It supports route exposure levels (`PUBLIC`, `PRIVATE`, `PROTECTED`) and handles authentication/authorization for protected routes.

---

## Table of Contents

- [Features](#features)
- [Configuration](#configuration)
- [Building the JAR](#building-the-jar)
- [Running the Application](#running-the-application)
- [Usage](#usage)

---

## Features

- Forward HTTP requests to microservices dynamically.
- Route exposure control:
    - **PUBLIC**: Accessible to anyone.
    - **PRIVATE**: Accessible only from internal network.
    - **PROTECTED**: Requires authentication.
- Reads microservice configuration from `application.yml`.
- Supports GET, POST, PUT, DELETE methods.
- Logs service and route information with color coding.

---
## Important For Authentication
to integrate your authentication service with the gateway, ensure route `auth` with `POST` method exist with specified url of `/aut/token`.
```imp
     - name: auth
      host: localhost
      port: 5030
      default-exposure: private
      routes:
        - path: /auth/token
          method: POST
          exposure: protected
```



---

## Configuration

The service is configured via `application.yml`. Example:

```yaml
spring:
  application:
    name: GatewayApplication

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
```
## Set Application.yml 
```aiignore
## Running the Gateway with a Custom YAML Configuration

By default, Spring Boot reads `application.yml` from `src/main/resources`.  
You can override it and provide a custom configuration file when running the JAR.

### Command

```bash
java -jar GatewayApplication.jar --spring.config.location=file:/full/path/to/application.yml
```
