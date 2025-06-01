# 🤝 Microvolunteer Platform

Платформа для з'єднання волонтерів та людей, які потребують допомоги.

## 🚀 Швидкий старт

### 1. Запуск для розробки

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Доступ до API

**🌐 Swagger UI:** http://localhost:8081/swagger-ui.html

**📚 API Documentation:** http://localhost:8081/v3/api-docs

**💾 H2 Console (dev режим):** http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:microvolunteer_dev`
- Username: `sa`
- Password: (пустий)

## 📋 API Endpoints

### 🔐 Authentication
- `POST /api/auth/register` - Регистрация пользователя
- `GET /api/auth/health` - Проверка auth сервиса

### 👤 Users
- `GET /api/users` - Список пользователей
- `GET /api/users/{id}` - Пользователь по ID
- `PUT /api/users/{id}` - Обновление пользователя

### 📋 Tasks
- `GET /api/tasks` - Все задания
- `POST /api/tasks` - Создать задание
- `GET /api/tasks/search` - Поиск заданий
- `GET /api/tasks/{id}` - Задание по ID
- `PUT /api/tasks/{id}` - Обновить задание
- `DELETE /api/tasks/{id}` - Удалить задание
- `PUT /api/tasks/{id}/complete` - Завершить задание

### 🏷️ Categories
- `GET /api/categories` - Все категории
- `GET /api/categories/active` - Активные категории
- `POST /api/categories` - Создать категорию (Admin)

### 🤝 Participations
- `POST /api/participations/join` - Присоединиться к заданию
- `DELETE /api/participations/leave` - Покинуть задание
- `GET /api/participations/task/{id}` - Участники задания
- `GET /api/participations/user/{id}` - Участие пользователя

## 🐳 Docker запуск

```bash
docker-compose up -d
```

## 🗄️ База даних

### Миграції Flyway
- `V1__Initial_schema.sql` - Початкова схема
- `V2__insert_test_data.sql` - Тестові дані

## 🔧 Конфігурація

### Профілі Spring
- **dev** - H2 база, детальні логи, Swagger включений
- **docker** - PostgreSQL, оптимізовані налаштування
- **test** - H2 in-memory для тестів

## 🧪 Тестування

```bash
./mvnw test
```

## 🛠️ Технології

- **Spring Boot 3.4.4** - Основний фреймворк
- **Spring Security** - Безпека та JWT
- **Spring Data JPA** - ORM та робота з БД
- **PostgreSQL** - Основна БД (продакшн)
- **H2** - БД для розробки та тестів
- **Flyway** - Міграції БД
- **Keycloak** - Управління користувачами
- **Swagger/OpenAPI 3** - Документація API
- **MapStruct** - Мапінг об'єктів
- **Docker** - Контейнеризація
- **Maven** - Збірка проекту
