# Shop Sphere

[![CI](https://github.com/harshitham1974/shop-sphere/actions/workflows/ci.yml/badge.svg)](https://github.com/harshitham1974/shop-sphere/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Language](https://img.shields.io/badge/Language-Java-blue)](https://www.java.com/)

Live demo: (https://shop-sphere-5sgv.onrender.com/swagger-ui/index.html) 
Note: hosted on Render's free tier, so the first request after a period of inactivity can take about a minute while the service wakes up.

# ShopSphere

A modular Spring Boot e-commerce backend providing user accounts, catalog management, cart and checkout, orders, payments, and secure JWT-protected APIs. ShopSphere is designed as a reusable foundation for online stores, with PostgreSQL persistence and a Dockerized local setup.

## Features

- **Authentication and authorization**: registration and login with JWT bearer tokens, role-based access (customer and admin)
- **User management**: profile, update profile, change password, delete account
- **Addresses**: full CRUD plus default-address selection
- **Catalog**: products and categories, with public read access and protected write operations
- **Reviews**: create, read, update, and delete product reviews, plus "my reviews"
- **Cart**: add, update, and remove items, or clear the cart
- **Orders**: place orders, view history, cancel orders
- **Payments**: create payments and look up payments by order
- **Wishlist**: add, view, and remove wishlist items
- **Admin dashboard**: order dashboard and order lifecycle management (confirm, processing, ship, deliver)
- **API docs**: interactive Swagger UI via Springdoc OpenAPI
- **Cross-cutting concerns**: centralized exception handling and AOP-based logging

## Tech Stack

| Area | Technology |
| --- | --- |
| Language | Java 25 |
| Framework | Spring Boot 4.1.0 |
| Persistence | Spring Data JPA, PostgreSQL |
| Security | Spring Security, JWT (JJWT) |
| API docs | Springdoc OpenAPI |
| Testing | JUnit, Spring test support, Testcontainers |
| Build and tooling | Maven (wrapper included), Docker, Docker Compose, GitHub Actions, Qodana |

## Project Structure

```
.
├── .github/                  CI workflows, issue and PR templates
├── src/
│   ├── main/
│   │   ├── java/com/ecom/shopsphere/
│   │   │   ├── aspect/       AOP logging
│   │   │   ├── config/       Security and OpenAPI configuration
│   │   │   ├── controller/   REST controllers
│   │   │   ├── dto/          Request and response DTOs
│   │   │   ├── entity/       JPA domain model
│   │   │   ├── exception/    Custom exceptions and global handler
│   │   │   ├── mapper/       Entity <-> DTO mappers
│   │   │   ├── repository/   Spring Data repositories
│   │   │   ├── security/     JWT filter, service, user details
│   │   │   ├── service/      Business logic (interfaces + impls)
│   │   │   └── ShopsphereApplication.java
│   │   └── resources/        Application configuration
│   └── test/                 Unit and integration tests
├── Dockerfile
├── docker-compose.yml        App + PostgreSQL
├── pom.xml
└── mvnw / mvnw.cmd
```

## Architecture

Requests follow a layered flow:

```
Client -> Controller -> Security (JWT filter) -> Service -> Repository -> PostgreSQL
                                   \-> Mapper (Entity <-> DTO)
```

- **Controllers** expose the REST API and handle validation and responses.
- **Security** authenticates each request via `JwtAuthenticationFilter` and authorizes access.
- **Services** hold business rules and transactional flows.
- **Repositories** persist JPA entities to PostgreSQL.
- **Mappers and DTOs** keep the API contract separate from the domain model.

### Domain model

User, Role, Address, Category, Product, Review, Cart, CartItem, Wishlist, WishlistItem, Order, OrderItem, OrderStatus, Payment, PaymentMethod, PaymentStatus.

## Getting Started

### Prerequisites

- Java 25
- Maven (or use the included wrapper)
- PostgreSQL, or Docker and Docker Compose

### Clone

```bash
git clone https://github.com/harshitham1974/shop-sphere.git
cd shop-sphere
```

### Configuration

Copy the example environment file and set your database and JWT values:

```bash
cp .env.example .env
```

Use `.env.docker` for the Docker Compose setup. At minimum, configure the database connection and the JWT secret.

### Run locally

```bash
./mvnw clean package -DskipTests
java -jar target/*.jar
```

### Run with Docker Compose

```bash
docker-compose up --build
```

This starts the application along with a local PostgreSQL instance.

## API Documentation

With the app running, open the Swagger UI provided by Springdoc OpenAPI (typically `http://localhost:8080/swagger-ui.html`; adjust host and port to your configuration).

## API Overview

All endpoints are prefixed with `/api/v1`. Protected endpoints require the header `Authorization: Bearer <token>`.

### Public

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/shopsphere/health` | Health check |
| POST | `/auth/register` | Register a new user |
| POST | `/auth/login` | Log in and receive a JWT |
| GET | `/products`, `/products/{productId}` | Browse products |
| GET | `/categories`, `/categories/{categoryId}` | Browse categories |

### Protected

| Area | Endpoints |
| --- | --- |
| Users | `GET /users/profile`, `PUT /users/update-profile`, `PUT /users/change-password`, `DELETE /users/delete-account` |
| Addresses | `POST /addresses`, `GET /addresses`, `GET /addresses/{addressId}`, `PUT /addresses/{addressId}`, `DELETE /addresses/{addressId}`, `PATCH /addresses/{addressId}/default` |
| Categories | `POST /categories`, `PUT /categories/{categoryId}`, `DELETE /categories/{categoryId}` |
| Products | `POST /products`, `PUT /products/{productId}`, `DELETE /products/{productId}` |
| Reviews | `POST /reviews`, `GET /reviews/{reviewId}`, `GET /reviews/product/{productId}`, `GET /reviews/my-reviews`, `PUT /reviews/{reviewId}`, `DELETE /reviews/{reviewId}` |
| Cart | `POST /cart/items`, `GET /cart`, `PUT /cart/items/{cartItemId}`, `DELETE /cart/items/{cartItemId}`, `DELETE /cart` |
| Orders | `POST /orders`, `GET /orders`, `GET /orders/{orderId}`, `PATCH /orders/{orderId}/cancel` |
| Payments | `POST /payments`, `GET /payments/order/{orderId}` |
| Wishlist | `POST /wishlist/items`, `GET /wishlist`, `DELETE /wishlist/items/{wishlistItemId}` |
| Admin | `GET /admin/orders/dashboard`, `GET /admin/orders`, `GET /admin/orders/{orderId}`, `PATCH /admin/orders/{orderId}/confirm`, `/processing`, `/ship`, `/deliver` |

## Testing

The project includes controller unit tests, service unit tests, and integration tests (using Testcontainers with PostgreSQL).

```bash
./mvnw test
```

Docker must be running for the integration tests.

## CI

GitHub Actions workflows run the build and tests (`ci.yml`) and Qodana code-quality analysis (`qodana_code_quality.yml`).

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before opening an issue or pull request.

## License

See the [License](License) file for details.
