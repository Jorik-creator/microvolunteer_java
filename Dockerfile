FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache tzdata curl wget && \
    cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime && \
    echo "Europe/Kiev" > /etc/timezone && \
    apk del tzdata

RUN addgroup -g 1001 -S microvolunteer && \
    adduser -S microvolunteer -u 1001 -G microvolunteer

RUN mkdir -p /app/logs && \
    chown -R microvolunteer:microvolunteer /app

COPY --from=build /app/target/microvolunteer-spring-1.0.0.jar app.jar
RUN chown microvolunteer:microvolunteer app.jar

USER microvolunteer

ENV JAVA_OPTS="-Xmx512m -Xms256m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=docker"

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
