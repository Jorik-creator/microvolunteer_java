# Micro-Volunteering REST API

REST API system "Micro-Volunteering" - platform for connecting volunteers and people who need help.

## Technologies

- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Security + JWT**
- **Spring Data JPA**
- **PostgreSQL 42.7.7**
- **Maven**
- **Lombok 1.18.38**
- **SpringDoc OpenAPI 2.8.9 (Swagger UI)**
- **Docker**

## Quick Start

### Using Docker

1. **Clone repository:**
   ```bash
   git clone <repository-url>
   cd Micro-Vulunteering
   ```

2. **Run with Docker Compose:**
   ```bash
   docker-compose up -d
   ```

3. **Check status:**
   ```bash
   docker-compose ps
   ```

4. **Access application:**
   - API: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - Health Check: http://localhost:8080/api/actuator/health

### Local Development

1. **Install dependencies:**
   - Java 21
   - Maven 3.8+
   - PostgreSQL

2. **Setup database:**
   ```sql
   CREATE DATABASE micro_volunteering;
   CREATE USER micro_volunteering WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE micro_volunteering TO micro_volunteering;
   ```

3. **Run application:**
   ```bash
   mvn clean package
   java -jar target/Micro-Vulunteering-1.0-SNAPSHOT.jar
   ```

## API Endpoints

### Authentication (`/api/auth`)
- `POST /auth/register` - User registration
- `POST /auth/login` - User login

### Users (`/api/users`)
- `GET /users` - List users (admin only)
- `GET /users/{id}` - Get user by ID (admin only)
- `GET /users/profile` - Current user profile
- `PUT /users/profile` - Update profile
- `GET /users/statistics` - User statistics

### Categories (`/api/categories`)
- `GET /categories` - List categories
- `GET /categories/{id}` - Get category by ID
- `POST /categories` - Create category (admin only)
- `PUT /categories/{id}` - Update category (admin only)
- `DELETE /categories/{id}` - Delete category (admin only)

### Tasks (`/api/tasks`)
- `GET /tasks` - List tasks with filters
- `GET /tasks/{id}` - Get task by ID
- `POST /tasks` - Create task
- `PUT /tasks/{id}` - Update task
- `DELETE /tasks/{id}` - Delete task
- `POST /tasks/{id}/join` - Join task (volunteers)
- `DELETE /tasks/{id}/leave` - Leave task
- `PATCH /tasks/{id}/complete` - Complete task
- `PATCH /tasks/{id}/cancel` - Cancel task

## Authentication

The system uses JWT tokens for authentication. After registration or login, you'll receive a token that must be passed in the header:

```
Authorization: Bearer <your-jwt-token>
```

## User Types

- **VOLUNTEER** - Volunteer (can join tasks)
- **VULNERABLE** - Vulnerable person (can create tasks)
- **ADMIN** - Administrator (can manage categories and users)

## Task Statuses

- **OPEN** - Open for joining
- **IN_PROGRESS** - In progress
- **COMPLETED** - Completed
- **CANCELLED** - Cancelled

## Testing

Run tests:
```bash
mvn test
```

Check code coverage:
```bash
mvn jacoco:report
```

## Development

### Maven Commands
```bash
mvn compile          # Compile
mvn test            # Run tests
mvn package         # Create JAR file
mvn clean           # Clean build artifacts
```

### Docker Commands
```bash
docker-compose up -d                    # Run in background
docker-compose down                     # Stop and remove containers
docker-compose logs app                 # View application logs
docker-compose exec postgres psql -U micro_volunteering -d micro_volunteering  # Connect to database
```

## Monitoring

- **Health Check**: `/api/actuator/health`
- **Metrics**: `/api/actuator/metrics`
- **Info**: `/api/actuator/info`

## Configuration

Main environment variables:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=micro_volunteering
DB_USERNAME=micro_volunteering
DB_PASSWORD=password
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

## Project Structure

```
src/
├── main/
│   ├── java/org/example/
│   │   ├── config/          # Spring configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Exception handling
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Repositories
│   │   ├── security/       # Security configuration
│   │   ├── service/        # Business logic
│   │   └── util/           # Utilities
│   └── resources/
│       ├── application.yml
│       └── application-docker.yml
└── test/                   # Tests
```

## License

MIT License