@echo off
echo Starting Microvolunteer in LOCAL mode (without Docker)...
echo.
echo IMPORTANT: Make sure you have:
echo - PostgreSQL running on localhost:5432
echo - Database 'microvolunteer_local' created
echo - Keycloak running on localhost:8080 (optional)
echo.
set SPRING_PROFILES_ACTIVE=local
mvnw.cmd spring-boot:run
