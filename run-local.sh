#!/bin/bash
echo "Starting Microvolunteer in LOCAL mode (without Docker)..."
echo ""
echo "IMPORTANT: Make sure you have:"
echo "- PostgreSQL running on localhost:5432"
echo "- Database 'microvolunteer_local' created"
echo "- Keycloak running on localhost:8080 (optional)"
echo ""
export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run
