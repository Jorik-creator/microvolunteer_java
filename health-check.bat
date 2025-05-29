@echo off
echo.
echo ========================================
echo   MICROVOLUNTEER PROJECT HEALTH CHECK
echo ========================================
echo.

echo 1. Checking Java version...
java -version
if %ERRORLEVEL% neq 0 (
    echo ❌ Java is not installed or not in PATH
    goto :end
)
echo ✅ Java is available
echo.

echo 2. Checking Maven wrapper...
if exist "mvnw.cmd" (
    echo ✅ Maven wrapper found
) else (
    echo ❌ Maven wrapper not found
    goto :end
)
echo.

echo 3. Checking Docker availability...
docker --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Docker is not installed or not running
) else (
    echo ✅ Docker is available
)
echo.

echo 4. Testing project compilation...
mvnw.cmd clean compile -q -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ❌ Project compilation failed!
    echo Check the error messages above
    goto :end
)
echo ✅ Project compiles successfully
echo.

echo 5. Checking Docker Compose files...
docker-compose -f docker-compose.yml config >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Production docker-compose.yml has errors
) else (
    echo ✅ Production docker-compose.yml is valid
)

docker-compose -f docker-compose.dev.yml config >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Development docker-compose.dev.yml has errors  
) else (
    echo ✅ Development docker-compose.dev.yml is valid
)
echo.

echo 6. Checking required files...
set FILES_OK=1

if not exist "src\main\java\com\microvolunteer\MicrovolunteerApplication.java" (
    echo ❌ Main application class not found
    set FILES_OK=0
)

if not exist "src\main\resources\application.yml" (
    echo ❌ Main application.yml not found
    set FILES_OK=0
)

if not exist "src\main\resources\application-dev.yml" (
    echo ❌ Development application-dev.yml not found
    set FILES_OK=0
)

if not exist "Dockerfile" (
    echo ❌ Dockerfile not found
    set FILES_OK=0
)

if %FILES_OK%==1 (
    echo ✅ All required files are present
) else (
    echo ❌ Some required files are missing
)
echo.

echo 7. Checking database migration files...
if exist "src\main\resources\db\migration\*.sql" (
    echo ✅ Database migration files found
) else (
    echo ❌ No database migration files found
)
echo.

echo ========================================
echo           SUMMARY
echo ========================================

echo Available run modes:
echo - Development with Docker: run-dev.bat
echo - Local without Docker: run-local.bat  
echo - Production with Docker: docker-compose up

echo.
echo Available commands:
echo - Quick test: quick-test.bat
echo - Health check: health-check.bat
echo - Manual compilation: mvnw.cmd clean compile

echo.
echo Ports in development mode:
echo - Application: http://localhost:8081
echo - Swagger UI: http://localhost:8081/swagger-ui.html
echo - Keycloak: http://localhost:8082
echo - PostgreSQL: localhost:5433
echo - Redis: localhost:6379

:end
echo.
echo Health check completed!
pause
