@echo off
echo Starting Microvolunteer in development mode...
set SPRING_PROFILES_ACTIVE=dev
mvnw.cmd spring-boot:run
