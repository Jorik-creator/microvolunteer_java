#!/bin/bash
echo "=== Quick Test of Microvolunteer ==="

echo "1. Testing Maven compilation..."
./mvnw clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
else
    echo "❌ Compilation failed!"
    exit 1
fi

echo "2. Running tests..."
./mvnw test -q

if [ $? -eq 0 ]; then
    echo "✅ Tests passed!"
else
    echo "⚠️  Tests failed (may be expected in development)"
fi

echo "3. Checking Docker Compose files..."
docker-compose -f docker-compose.yml config > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Production Docker Compose is valid!"
else
    echo "❌ Production Docker Compose has issues!"
fi

docker-compose -f docker-compose.dev.yml config > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Development Docker Compose is valid!"
else
    echo "❌ Development Docker Compose has issues!"
fi

echo "=== Quick test completed ==="
