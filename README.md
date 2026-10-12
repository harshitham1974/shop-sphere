# Shop Sphere

[![CI](https://github.com/harshitham1974/shop-sphere/actions/workflows/ci.yml/badge.svg)](https://github.com/harshitham1974/shop-sphere/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Language](https://img.shields.io/badge/Language-Java-blue)](https://www.java.com/)

Live demo: (https://shop-sphere-5sgv.onrender.com/swagger-ui/index.html) 
Note: hosted on Render's free tier, so the first request after a period of inactivity can take about a minute while the service wakes up.


A modern e‑commerce platform built from scratch using industry-standard practices. Shop Sphere aims to provide a modular, extensible, and secure foundation for online stores.

## Table of Contents
- About
- Features
- Tech stack
- Getting started
  - Prerequisites
  - Build (Maven & Gradle)
  - Run locally
  - Docker
- Running tests
- Environment variables
- Project structure
- Contributing
- Code of Conduct
- Security
- License

## About
Shop Sphere is designed to be a production-ready e-commerce backend with modular services, secure auth, product/catalog management, checkout, and integrations for payments and shipping.

## Features
- User accounts (registration, login, roles)
- Product catalog with categories and tags
- Shopping cart and checkout flow
- Order management and history
- Admin dashboard (planned)
- RESTful JSON APIs, with OpenAPI/Swagger (planned)
- Dockerized for easy deployment

## Tech stack
- Java (>=17 recommended)
- Spring Boot (suggested)
- JPA / Hibernate
- PostgreSQL (recommended)
- Docker / Docker Compose
- GitHub Actions for CI

## Getting started

### Prerequisites
- JDK 17+
- Maven 3.6+ or Gradle 7+
- Docker (for container usage)
- Git

### Clone the repo
git clone https://github.com/harshitham1974/shop-sphere.git
cd shop-sphere

### Build

Maven:
mvn clean package -DskipTests

Gradle:
./gradlew clean build -x test

(If you use Maven, the packaged jar will typically be in `target/`. For Gradle, it will be in `build/libs/`.)

### Run locally (jar)
java -jar target/shop-sphere-0.0.1-SNAPSHOT.jar
# or for Gradle build
java -jar build/libs/shop-sphere-0.0.1-SNAPSHOT.jar

By default the app listens on port 8080. Configure via `application.properties` or environment variables.

### Docker (build & run)
Build:
docker build -t shop-sphere:latest .

Run:
docker run -p 8080:8080 --env-file .env.example shop-sphere:latest

### Docker Compose (example)
Create a `docker-compose.yml` to run app + postgres for local development. Use `.env.example` as a starting point.

## Running tests
Maven:
mvn test

Gradle:
./gradlew test

## Environment variables
See `.env.example` for required environment variables (DB connection, JWT secret, etc.). For production, use a secure secrets provider.

## Project structure (suggested)
- src/main/java — application code
- src/main/resources — configuration
- src/test — tests
- docs/ — documentation
- infra/ — docker-compose, k8s manifests, etc.

## Contributing
We welcome contributions! See CONTRIBUTING.md for guidelines on how to contribute, tests to run, commit style, and how to open PRs.

## Code of Conduct
Please follow the CODE_OF_CONDUCT.md file. Be respectful and inclusive.

## Security
If you discover a security vulnerability, please report it privately via the repository's security contact or email.

## License
This project is licensed under the MIT License — see the LICENSE file for details.
