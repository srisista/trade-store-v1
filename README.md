# Trade Store Application

A Spring Boot application for managing trades with MongoDB as the database.

## Features

- Store trades with versioning
- Retrieve trades by ID and version
- Automatic trade expiration
- RESTful API endpoints
- Swagger/OpenAPI documentation
- Comprehensive test coverage
- CI/CD pipeline with GitHub Actions

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- MongoDB 4.4 or higher

## Getting Started

1. Clone the repository:
```bash
git clone https://github.com/yourusername/trade-store.git
cd trade-store
```

2. Build the application:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on http://localhost:8080

## API Documentation

Swagger UI is available at: http://localhost:8080/swagger-ui.html

## Testing

Run all tests:
```bash
mvn test
```

Run integration tests:
```bash
mvn verify -P integration-test
```

## CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment. The pipeline includes:

- Build and test
- Code coverage reporting
- Dependency vulnerability scanning
- Artifact uploads

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── tradestore/
│   │           ├── api/
│   │           ├── domain/
│   │           └── infrastructure/
│   └── resources/
└── test/
    ├── java/
    │   └── com/
    │       └── tradestore/
    └── resources/
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.