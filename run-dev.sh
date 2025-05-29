#!/bin/bash
echo "Starting Microvolunteer in development mode..."
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
