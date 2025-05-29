#!/bin/bash

echo
echo "========================================"
echo "   MICROVOLUNTEER PROJECT HEALTH CHECK"
echo "========================================"
echo

echo "1. Checking Java version..."
if java -version >/dev/null 2>&1; then
    java -version
    echo "✅ Java is available"
else
    echo "❌ Java is not installed or not in PATH"
    exit 1
fi
echo

echo "2. Checking Maven wrapper..."
if [ -f "mvnw" ]; then
    echo "✅ Maven wrapper found"
else
    echo "❌ Maven wrapper not found"
    exit 1
fi
echo

echo "3. Checking Docker availability..."
if docker --version >/dev/null 2>&1; then
    echo "✅ Docker is available"
else
    echo "❌ Docker is not installed or not running"
fi
echo

echo "4. Testing project compilation..."
if ./mvnw clean compile -q -DskipTests; then
    echo "✅ Project compiles successfully"
else
    echo "❌ Project compilation failed!"
    echo "Check the error messages above"
    exit 1
fi
echo

echo "5. Checking Docker Compose files..."
if docker-compose -f docker-compose.yml config >/dev/null 2>&1; then
    echo "✅ Production docker-compose.yml is valid"
else
    echo "❌ Production docker-compose.yml has errors"
fi

if docker-compose -f docker-compose.dev.yml config >/dev/null 2>&1; then
    echo "✅ Development docker-compose.dev.yml is valid"
else
    echo "❌ Development docker-compose.dev.yml has errors"
fi
echo

echo "6. Checking required files..."
FILES_OK=1

if [ ! -f "src/main/java/com/microvolunteer/MicrovolunteerApplication.java" ]; then
    echo "❌ Main application class not found"
    FILES_OK=0
fi

if [ ! -f "src/main/resources/application.yml" ]; then
    echo "❌ Main application.yml not found"  
    FILES_OK=0
fi

if [ ! -f "src/main/resources/application-dev.yml" ]; then
    echo "❌ Development application-dev.yml not found"
    FILES_OK=0
fi

if [ ! -f "Dockerfile" ]; then
    echo "❌ Dockerfile not found"
    FILES_OK=0
fi

if [ $FILES_OK -eq 1 ]; then
    echo "✅ All required files are present"
else
    echo "❌ Some required files are missing"
fi
echo

echo "7. Checking database migration files..."
if ls src/main/resources/db/migration/*.sql >/dev/null 2>&1; then
    echo "✅ Database migration files found"
else
    echo "❌ No database migration files found"
fi
echo

echo "========================================"
echo "           SUMMARY"
echo "========================================"

echo "Available run modes:"
echo "- Development with Docker: ./run-dev.sh"
echo "- Local without Docker: ./run-local.sh"
echo "- Production with Docker: docker-compose up"

echo
echo "Available commands:"
echo "- Quick test: ./quick-test.sh"
echo "- Health check: ./health-check.sh" 
echo "- Manual compilation: ./mvnw clean compile"

echo
echo "Ports in development mode:"
echo "- Application: http://localhost:8081"
echo "- Swagger UI: http://localhost:8081/swagger-ui.html"
echo "- Keycloak: http://localhost:8082"
echo "- PostgreSQL: localhost:5433"
echo "- Redis: localhost:6379"

echo
echo "Health check completed!"
