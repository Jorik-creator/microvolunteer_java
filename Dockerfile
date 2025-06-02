# Multi-stage build for optimized image size
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and configuration files
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Production stage with minimal JRE
FROM eclipse-temurin:21-jre-alpine

# Create non-root user for security
RUN addgroup -g 1001 microvolunteer && \
    adduser -D -s /bin/sh -u 1001 -G microvolunteer microvolunteer

# Set working directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R microvolunteer:microvolunteer /app

# Copy the built JAR file from builder stage
COPY --from=builder /app/target/microvolunteer-*.jar app.jar

# Change ownership to non-root user
RUN chown microvolunteer:microvolunteer app.jar

# Switch to non-root user
USER microvolunteer

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
