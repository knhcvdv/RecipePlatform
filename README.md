# Recipe Platform

A secure and modern recipe management platform built with Spring Boot.

## Features

- User authentication and authorization with JWT
- Role-based access control (USER, MODERATOR, ADMIN)
- Recipe management with categories
- Comprehensive API documentation with Swagger/OpenAPI
- Robust error handling and logging
- Secure endpoints with proper authorization
- Docker support for easy deployment

## Prerequisites

- JDK 11
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 13 (if running locally)

## Getting Started

### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/recipe-platform.git
   cd recipe-platform
   ```

2. Configure the database:
   - Create a PostgreSQL database named `recipe_platform`
   - Update `application.properties` with your database credentials if different from defaults

3. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### Docker Deployment

1. Build and run using Docker Compose:
   ```bash
   docker-compose up --build
   ```

The application will be available at `http://localhost:8080`

## API Documentation

Once the application is running, you can access the Swagger UI at:
- `http://localhost:8080/swagger-ui.html`

## Authentication

The API uses JWT for authentication. To access protected endpoints:

1. Register a new user:
   ```bash
   POST /api/auth/signup
   {
     "username": "user",
     "email": "user@example.com",
     "password": "password123",
     "roles": ["user"]
   }
   ```

2. Login to get a JWT token:
   ```bash
   POST /api/auth/signin
   {
     "username": "user",
     "password": "password123"
   }
   ```

3. Use the token in the Authorization header:
   ```
   Authorization: Bearer <your_jwt_token>
   ```

## API Endpoints

### Public Endpoints
- `GET /api/recipes` - Get all recipes
- `GET /api/recipes/{id}` - Get recipe by ID
- `GET /api/recipes/search` - Search recipes by title
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID

### Protected Endpoints

#### User Role
- `POST /api/recipes` - Create a new recipe

#### Moderator Role
- `POST /api/categories` - Create a new category
- `PUT /api/categories/{id}` - Update a category
- `PUT /api/recipes/{id}` - Update a recipe

#### Admin Role
- `DELETE /api/recipes/{id}` - Delete a recipe
- `DELETE /api/categories/{id}` - Delete a category

## Testing

Run the test suite:
```bash
mvn test
```

## CI/CD Pipeline

The project includes a GitHub Actions workflow that:
1. Builds the application
2. Runs tests
3. Generates test and coverage reports
4. Builds and pushes Docker image (on main branch)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 