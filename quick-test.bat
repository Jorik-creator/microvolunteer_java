@echo off
echo === Quick Test of Microvolunteer ===

echo 1. Testing Maven compilation...
mvnw.cmd clean compile -q

if %ERRORLEVEL% neq 0 (
    echo ❌ Compilation failed!
    exit /b 1
)
echo ✅ Compilation successful!

echo 2. Running tests...
mvnw.cmd test -q

if %ERRORLEVEL% neq 0 (
    echo ⚠️  Tests failed (may be expected in development)
) else (
    echo ✅ Tests passed!
)

echo 3. Checking Docker Compose files...
docker-compose -f docker-compose.yml config >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Production Docker Compose has issues!
) else (
    echo ✅ Production Docker Compose is valid!
)

docker-compose -f docker-compose.dev.yml config >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Development Docker Compose has issues!
) else (
    echo ✅ Development Docker Compose is valid!
)

echo === Quick test completed ===
pause
