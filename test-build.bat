@echo off
echo "=== Testing Microvolunteer Build ==="

echo "1. Stopping and cleaning up containers..."
docker-compose -f docker-compose.dev.yml down -v

echo "2. Starting infrastructure..."
docker-compose -f docker-compose.dev.yml up -d

echo "3. Waiting for database to be ready..."
timeout /t 30

echo "4. Testing Maven build..."
mvnw.cmd clean compile -DskipTests

if %ERRORLEVEL% neq 0 (
    echo "Build failed!"
    exit /b 1
)

echo "5. Running application..."
set SPRING_PROFILES_ACTIVE=dev
mvnw.cmd spring-boot:run

echo "=== Build and test completed ==="
