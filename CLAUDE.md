# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a fully implemented Java Spring Boot REST API project for a Micro-Volunteering application ("Мікро-Волонтерство"), structured as a coursework project ("cursova"). The system connects volunteers with people who need help through a comprehensive task management platform.

## Architecture

- **Build System**: Maven with Spring Boot parent
- **Java Version**: 21 (source and target)
- **Framework**: Spring Boot 3.4.1
- **Database**: PostgreSQL 17.5
- **Security**: Spring Security with JWT authentication
- **Documentation**: OpenAPI 3 (Swagger UI)
- **Containerization**: Docker with docker-compose
- **Package Structure**: `org.example` base package
- **Entry Point**: `src/main/java/org/example/MicroVolunteeringApplication.java`

## Technology Stack

- **Spring Boot 3.5.0** - Main framework
- **Spring Security + JWT** - Authentication and authorization  
- **Spring Data JPA** - Data persistence
- **PostgreSQL 17.5** - Primary database
- **Lombok 1.18.38** - Code generation
- **SpringDoc OpenAPI 2.8.9** - API documentation (Swagger UI)
- **JJWT 0.12.6** - JWT token handling
- **Liquibase** - Database migrations
- **JUnit 5** - Testing framework
- **Docker** - Containerization

## Development Commands

**Prerequisites**: Maven 3.8+, Java 21, Docker (for containerized setup)

### Local Development Commands
```bash
mvn compile          # Compile the project
mvn clean compile    # Clean and compile
mvn package          # Create JAR file
mvn test            # Run all tests
mvn spring-boot:run  # Run the application locally
```

### Docker Commands (Recommended)
```bash
docker-compose up -d        # Start application with PostgreSQL
docker-compose down         # Stop and remove containers
docker-compose logs app     # View application logs
docker-compose ps          # Check container status
```

### Database Commands
```bash
# Connect to PostgreSQL in Docker
docker-compose exec postgres psql -U micro_volunteering -d micro_volunteering
```

## Project Structure

```
src/
├── main/
│   ├── java/org/example/
│   │   ├── config/          # Spring configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Exception handling
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data repositories
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── application.yml      # Main configuration
│       └── application-docker.yml # Docker configuration
└── test/
    └── java/                # Unit and integration tests
```

## API Endpoints

### Authentication (`/api/auth`)
- `POST /auth/register` - User registration
- `POST /auth/login` - User login

### Users (`/api/users`)
- `GET /users` - List users (admin only)
- `GET /users/profile` - Current user profile
- `PUT /users/profile` - Update profile
- `GET /users/statistics` - User statistics

### Categories (`/api/categories`)
- `GET /categories` - List categories
- `POST /categories` - Create category (admin only)
- `PUT /categories/{id}` - Update category (admin only)
- `DELETE /categories/{id}` - Delete category (admin only)

### Tasks (`/api/tasks`)
- `GET /tasks` - List tasks with filters
- `POST /tasks` - Create task
- `PUT /tasks/{id}` - Update task
- `DELETE /tasks/{id}` - Delete task
- `POST /tasks/{id}/join` - Join task (volunteers)
- `DELETE /tasks/{id}/leave` - Leave task
- `PATCH /tasks/{id}/complete` - Complete task
- `PATCH /tasks/{id}/cancel` - Cancel task

## User Types and Permissions

- **VOLUNTEER** - Can join tasks, view all tasks
- **VULNERABLE** - Can create tasks requesting help
- **ADMIN** - Can manage categories and users

## Development Notes

### Running the Application

**Option 1: Docker (Recommended)**
```bash
docker-compose up -d
```
Access: http://localhost:8080/api/swagger-ui.html

**Option 2: Local Development**
1. Start PostgreSQL locally
2. Update `application.yml` with your database credentials
3. Run: `mvn spring-boot:run`

### Testing
- Run tests: `mvn test`
- Code coverage: `mvn jacoco:report`
- Integration tests included for controllers and services

### API Documentation
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/api-docs

### Database Schema
- Auto-generated via JPA/Hibernate
- Sample categories initialized on startup
- Full entity relationships implemented

### Security
- JWT token-based authentication
- Role-based authorization
- Password encryption with BCrypt
- CORS configured for development

### Monitoring
- Health check: `/api/actuator/health`  
- Metrics: `/api/actuator/metrics`

## Common Development Tasks

### Adding New Endpoints
1. Create DTO classes in `dto/` package
2. Add service methods in appropriate service class
3. Create controller methods with proper annotations
4. Add security configuration if needed
5. Write unit tests

### Database Changes
- Modify entity classes in `model/` package
- Update repository interfaces if needed
- Hibernate will auto-update schema in development

### Adding New User Roles
1. Update `UserType` enum
2. Modify security configuration in `SecurityConfig`
3. Update controller method security annotations

## Troubleshooting

### Common Issues
- **Port 8080 in use**: Change server port in `application.yml`
- *1/*Database connection**: Verify PostgreSQL is running and credentials are correct
- **JWT issues**: Check JWT secret configuration
- **Docker issues**: Ensure Docker daemon is running

### Logs
- Application logs: `docker-compose logs app`
- Database logs: `docker-compose logs postgres`

## Testing Strategy

The project includes:
- Unit tests for service layer
- Integration tests for controllers
- Security tests for authentication
- Repository tests for data access
- Target coverage: 70%+

## Available AI Agents

### General-Purpose Agent
- **Type**: `general-purpose`
- **Capabilities**: 
  - Complex multi-step research tasks
  - Code searching and analysis
  - File pattern matching and exploration
  - Comprehensive codebase investigation
- **When to Use**: 
  - Open-ended searches requiring multiple rounds of exploration
  - Complex debugging that requires systematic investigation
  - Large-scale code refactoring planning
  - Understanding unfamiliar codebases or patterns
- **Tools Available**: All available tools (*, Bash, Read, Grep, Glob, etc.)

### Usage Examples
```
# Use general-purpose agent for complex searches
"Search through the entire codebase to find all authentication-related code and analyze security patterns"

# Use for multi-step debugging
"Investigate all 500 errors across endpoints, identify root causes, and suggest fixes"

# Use for architectural analysis  
"Analyze the service layer architecture and identify potential performance bottlenecks"
```