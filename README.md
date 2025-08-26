# Micro-Volunteering REST API

REST API система "Мікро-Волонтерство" - платформа для об'єднання волонтерів та людей, які потребують допомоги.

## Технології

- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Security + JWT**
- **Spring Data JPA**
- **PostgreSQL 17.5**
- **Maven**
- **Lombok 1.18.38**
- **SpringDoc OpenAPI 2.8.9 (Swagger UI)**
- **Docker**

## Старт

### Використання Docker

1. **Клонування репозиторію:**
   ```bash
   git clone <repository-url>
   cd Micro-Vulunteering
   ```

2. **Запуск за допомогою Docker Compose:**
   ```bash
   docker-compose up -d
   ```

3. **Перевірка статусу:**
   ```bash
   docker-compose ps
   ```

4. **Доступ до додатку:**
   - API: http://localhost:8080/api
   - Swagger UI: http://localhost:8080/api/swagger-ui.html
   - Health Check: http://localhost:8080/api/actuator/health

### Локальний запуск

1. **Встановіть залежності:**
   - Java 21
   - Maven 3.8+
   - PostgreSQL 17.5

2. **Налаштуйте базу даних:**
   ```sql
   CREATE DATABASE micro_volunteering;
   CREATE USER micro_volunteering WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE micro_volunteering TO micro_volunteering;
   ```

3. **Запустіть додаток:**
   ```bash
   mvn clean package
   java -jar target/Micro-Vulunteering-1.0-SNAPSHOT.jar
   ```

## API Endpoints

### Аутентифікація (`/api/auth`)
- `POST /auth/register` - Реєстрація користувача
- `POST /auth/login` - Вхід користувача

### Користувачі (`/api/users`)
- `GET /users` - Список користувачів (тільки адмін)
- `GET /users/{id}` - Користувач за ID (тільки адмін)
- `GET /users/profile` - Профіль поточного користувача
- `PUT /users/profile` - Оновлення профілю
- `GET /users/statistics` - Статистика користувача

### Категорії (`/api/categories`)
- `GET /categories` - Список категорій
- `GET /categories/{id}` - Категорія за ID
- `POST /categories` - Створення категорії (тільки адмін)
- `PUT /categories/{id}` - Оновлення категорії (тільки адмін)
- `DELETE /categories/{id}` - Видалення категорії (тільки адмін)

### Завдання (`/api/tasks`)
- `GET /tasks` - Список завдань з фільтрами
- `GET /tasks/{id}` - Завдання за ID
- `POST /tasks` - Створення завдання
- `PUT /tasks/{id}` - Оновлення завдання
- `DELETE /tasks/{id}` - Видалення завдання
- `POST /tasks/{id}/join` - Приєднання до завдання (волонтери)
- `DELETE /tasks/{id}/leave` - Вихід із завдання
- `PATCH /tasks/{id}/complete` - Завершення завдання
- `PATCH /tasks/{id}/cancel` - Скасування завдання

## Аутентифікація

Система використовує JWT токени для аутентифікації. Після реєстрації або входу ви отримаєте токен, який потрібно передавати в заголовку:

```
Authorization: Bearer <your-jwt-token>
```

## Типи користувачів

- **VOLUNTEER** - Волонтер (може приєднуватись до завдань)
- **VULNERABLE** - Вразлива людина (може створювати завдання)
- **ADMIN** - Адміністратор (може управляти категоріями та користувачами)

## Статуси завдань

- **OPEN** - Відкрито для приєднання
- **IN_PROGRESS** - В процесі виконання
- **COMPLETED** - Завершено
- **CANCELLED** - Скасовано

## Тестування

Запуск тестів:
```bash
mvn test
```

Перевірка покриття коду:
```bash
mvn jacoco:report
```

## Розробка

### Команди Maven
```bash
mvn compile          # Компіляція
mvn test            # Запуск тестів
mvn package         # Створення JAR файлу
mvn clean           # Очищення build артефактів
```

### Команди Docker
```bash
docker-compose up -d                    # Запуск в фоновому режимі
docker-compose down                     # Зупинка та видалення контейнерів
docker-compose logs app                 # Перегляд логів додатку
docker-compose exec postgres psql -U micro_volunteering -d micro_volunteering  # Підключення до БД
```

## Моніторинг

- **Health Check**: `/api/actuator/health`
- **Metrics**: `/api/actuator/metrics`
- **Info**: `/api/actuator/info`

## Конфігурація

Основні змінні середовища:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=micro_volunteering
DB_USERNAME=micro_volunteering
DB_PASSWORD=password
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
```

## Структура проекту

```
src/
├── main/
│   ├── java/org/example/
│   │   ├── config/          # Конфігурація Spring
│   │   ├── controller/      # REST контролери
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Обробка винятків
│   │   ├── model/          # JPA сутності
│   │   ├── repository/     # Репозиторії
│   │   ├── security/       # Налаштування безпеки
│   │   ├── service/        # Бізнес-логіка
│   │   └── util/           # Утиліти
│   └── resources/
│       ├── application.yml
│       └── application-docker.yml
└── test/                   # Тести
```

## Ліцензія

MIT License