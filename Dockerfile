# Multi-stage build для оптимізації розміру образу
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Копіюємо файли Maven для кешування залежностей
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Завантажуємо залежності (окремий шар для кешування)
RUN ./mvnw dependency:go-offline -B

# Копіюємо вихідний код та збираємо додаток
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Другий етап - runtime образ
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Встановлення часового поясу та корисних утиліт
RUN apk add --no-cache tzdata curl wget && \
    cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime && \
    echo "Europe/Kiev" > /etc/timezone && \
    apk del tzdata

# Створення непривілегійованого користувача для безпеки
RUN addgroup -g 1001 -S microvolunteer && \
    adduser -S microvolunteer -u 1001 -G microvolunteer

# Створення необхідних директорій
RUN mkdir -p /app/logs && \
    chown -R microvolunteer:microvolunteer /app

# Копіювання JAR файлу з build stage
COPY --from=build /app/target/microvolunteer-spring-1.0.0.jar app.jar
RUN chown microvolunteer:microvolunteer app.jar

# Переключення на непривілегійованого користувача
USER microvolunteer

# Налаштування JVM для контейнера
ENV JAVA_OPTS="-Xmx512m -Xms256m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.profiles.active=docker"

# Відкриття порту
EXPOSE 8081

# Health check для контролю стану контейнера
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Точка входу з обробкою сигналів
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
