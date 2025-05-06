# Trade Store Application

A Spring Boot application for managing trades with Kafka integration for event streaming.

## Features

- Store trades with versioning
- Retrieve trades by ID and version
- Automatic trade expiration
- RESTful API endpoints
- Swagger/OpenAPI documentation
- Comprehensive test coverage
- CI/CD pipeline with GitHub Actions

## Prerequisites

- Java 17
- Maven 3.8+
- Docker Desktop
- PowerShell (for Windows users)

## Required Services

The application requires the following services:

- PostgreSQL 13
- MongoDB 4.0.21
- Apache Kafka 7.3.0

## Quick Start

1. Start Required Services:
   ```powershell
   ./start-services.ps1
   ```
   This script will start all required Docker containers with the following configurations:
   - PostgreSQL: localhost:5432 (credentials: postgres/postgres)
   - MongoDB: localhost:27017
   - Kafka: localhost:9092

2. Build the Application:
   ```bash
   mvn clean install
   ```

3. Run the Application:
   ```bash
   mvn spring-boot:run
   ```

## Configuration

### Application Properties
The application configuration is in `src/main/resources/application.yml`:
- Database configurations
- Kafka settings
- Logging levels
- Server configurations

### Dependencies
Key dependencies include:
- Spring Boot 3.2.3
- Spring Kafka 3.1.1
- Jetty (Web Server)
- PostgreSQL Driver
- MongoDB Driver
- SpringDoc OpenAPI UI

## API Documentation

Once the application is running, you can access:
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## Monitoring

The application exposes various actuator endpoints:
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

## Development

### Testing
The project uses:
- JUnit 5 for unit tests
- Testcontainers for integration tests
- Embedded Kafka for Kafka integration tests

Run tests with:
```bash
mvn test
```

### Code Quality
The project includes:
- Jacoco for code coverage
- OWASP dependency check
- Lombok for reducing boilerplate
- MapStruct for object mapping

## Troubleshooting

1. Docker Services:
   - Verify services are running: `docker ps`
   - Check logs: `docker logs <container-name>`

2. Application Startup:
   - Ensure all required ports are available
   - Check application logs in `logs/` directory

3. Common Issues:
   - If Kafka fails to start, ensure Zookeeper is running
   - For database connection issues, verify credentials in application.yml

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.