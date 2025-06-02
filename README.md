# MicroVolunteer Backend

Курсовий бекенд-проєкт «MicroVolunteer» - це мінімальна, але повністю працездатна серверна частина сервісу, що з'єднує дві категорії користувачів: уразливих людей, які публікують прохання про допомогу, і волонтерів, які ці прохання виконують.

## 🏗️ Технологічний стек

- **Java 21** - основна мова програмування
- **Spring Boot 3.5.0** - основний фреймворк
- **Spring Web** - для REST API
- **Spring Data JPA + Hibernate** - для роботи з базою даних
- **PostgreSQL 17** - основна база даних
- **Spring Security** - як ресурс-сервер з JWT автентифікацією
- **Keycloak 26.2.5** - сервер ідентифікації та авторизації
- **Flyway** - для міграцій схеми бази даних
- **OpenAPI 3 (Swagger)** - для документації API
- **MapStruct** - для маппінгу між DTO та Entity
- **Maven** - система збірки
- **Docker & Docker Compose** - для контейнеризації
- **JUnit 5 + Mockito** - для unit тестування
- **Testcontainers + RestAssured** - для інтеграційного тестування

## 🚀 Швидкий старт

### Вимоги

- **JDK 21** або новіша версія
- **Docker** та **Docker Compose**
- **Git**

### 1. Клонування репозиторію

```bash
git clone <repository-url>
cd microvolunteer
```

### 2. Запуск за допомогою Docker Compose

```bash
# Запуск всіх сервісів
docker compose up -d

# Перевірка статусу сервісів
docker compose ps

# Перегляд логів
docker compose logs -f microvolunteer-app
```

### 3. Перевірка роботи

Після запуску сервіси будуть доступні за наступними адресами:

- **MicroVolunteer API**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Keycloak Admin Console**: http://localhost:8080 (admin/admin123)

### 4. Тестування API

Перейдіть до Swagger UI за адресою http://localhost:8081/swagger-ui.html та:

1. Натисніть кнопку **"Authorize"**
2. Увійдіть через Keycloak використовуючи тестові облікові записи:
   - **Адмін**: admin/admin123
   - **Волонтер**: volunteer/volunteer123  
   - **Уразлива особа**: sensitive/sensitive123

## 🏛️ Архітектура додатку

### Шарована архітектура

```
┌─────────────────┐
│   Controllers   │ ← REST API шар
├─────────────────┤
│    Services     │ ← Бізнес-логіка
├─────────────────┤
│  Repositories   │ ← Доступ до даних
├─────────────────┤
│    Entities     │ ← JPA моделі
└─────────────────┘
```

### Основні компоненти

- **Controllers**: Обробляють HTTP запити, валідують дані, повертають відповіді
- **Services**: Містять бізнес-логіку, керують транзакціями
- **Repositories**: Spring Data JPA інтерфейси для CRUD операцій
- **Entities**: JPA сутності що представляють таблиці БД
- **DTOs**: Об'єкти передачі даних для API
- **Mappers**: MapStruct мапери для конвертації між Entity та DTO

## 📊 Схема бази даних

```sql
users
├── id (PK)
├── keycloak_subject (UNIQUE)
├── first_name
├── last_name  
├── email (UNIQUE)
├── phone
├── description
├── user_type (VOLUNTEER/SENSITIVE/ADMIN)
├── active
├── created_at
└── updated_at

tasks
├── id (PK)
├── title
├── description
├── location
├── deadline
├── status (OPEN/IN_PROGRESS/COMPLETED)
├── author_id (FK → users.id)
├── created_at
├── updated_at
└── completed_at

categories
├── id (PK)
├── name (UNIQUE)
├── description
├── active
├── created_at
└── updated_at

task_categories (Many-to-Many)
├── task_id (FK → tasks.id)
└── category_id (FK → categories.id)

participations
├── id (PK)
├── volunteer_id (FK → users.id)
├── task_id (FK → tasks.id)
├── active
├── notes
├── joined_at
├── updated_at
└── left_at
```

## 🔐 Автентифікація та авторизація

### Ролі користувачів

- **ROLE_ADMIN**: Повний доступ до системи, керування категоріями
- **ROLE_SENSITIVE**: Створення завдань, позначення їх як виконаних
- **ROLE_VOLUNTEER**: Участь у завданнях, перегляд своєї статистики

### Налаштування Keycloak

Realm `microvolunteer` містить:
- Три основні ролі
- Клієнт `microvolunteer-app` 
- Тестові користувачі для кожної ролі
- JWT токени з включеними ролями в claim `realm_access.roles`

## 📡 REST API ендпоїнти

### Користувачі

- `POST /api/users/register` - Реєстрація нового користувача
- `GET /api/users/me` - Отримання профілю поточного користувача
- `PUT /api/users/me` - Оновлення профілю
- `GET /api/users/{id}` - Отримання користувача за ID
- `GET /api/users/volunteers` - Список волонтерів

### Завдання

- `POST /api/tasks` - Створення завдання (тільки SENSITIVE)
- `GET /api/tasks` - Пошук завдань з фільтрами та пагінацією
- `GET /api/tasks/{id}` - Отримання завдання за ID
- `PATCH /api/tasks/{id}/complete` - Позначення як виконане
- `DELETE /api/tasks/{id}` - Видалення завдання
- `GET /api/tasks/my` - Мої завдання

### Участь волонтерів

- `POST /api/tasks/{id}/participate` - Приєднатися до завдання
- `DELETE /api/tasks/{id}/participate` - Покинути завдання
- `GET /api/tasks/{id}/volunteers` - Волонтери завдання
- `GET /api/participations/my-tasks` - Мої участі
- `GET /api/participations/my-statistics` - Моя статистика

### Категорії

- `GET /api/categories` - Список активних категорій
- `POST /api/categories` - Створення категорії (тільки ADMIN)
- `PUT /api/categories/{id}` - Оновлення категорії (тільки ADMIN)
- `DELETE /api/categories/{id}` - Деактивація категорії (тільки ADMIN)

## 🧪 Тестування

### Unit тести

```bash
# Запуск всіх unit тестів
./mvnw test

# Запуск тестів конкретного класу
./mvnw test -Dtest=UserServiceTest
```

### Інтеграційні тести

```bash
# Запуск інтеграційних тестів (використовують Testcontainers)
./mvnw verify

# Тести з покриттям коду
./mvnw clean verify jacoco:report
```

## 🐳 Docker

### Локальна розробка

```bash
# Запуск тільки залежностей (PostgreSQL + Keycloak)
docker compose up postgres keycloak -d

# Запуск додатку локально
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Повна збірка

```bash
# Збірка образу
docker build -t microvolunteer:latest .

# Запуск всіх сервісів
docker compose up -d
```

## 🔧 Розробка

### Налаштування IDE

1. Імпортуйте проєкт як Maven проєкт
2. Встановіть JDK 21
3. Увімкніть annotation processing для MapStruct
4. Налаштуйте code style згідно з .editorconfig

### Профілі Spring

- `dev` - для локальної розробки
- `docker` - для запуску в контейнерах
- `test` - для тестування

### Структура проєкту

```
src/main/java/com/microvolunteer/
├── config/           # Конфігурація Spring
├── controller/       # REST контролери
├── dto/             # Data Transfer Objects
├── entity/          # JPA сутності
├── enums/           # Енумерації
├── exception/       # Обробка винятків
├── mapper/          # MapStruct мапери
├── repository/      # Spring Data JPA репозиторії
└── service/         # Бізнес-логіка

src/main/resources/
├── db/migration/    # Flyway міграції
├── application.yml  # Основна конфігурація
├── application-dev.yml
└── application-docker.yml

src/test/java/       # Тести
```

## 📝 Приклади запитів

### Отримання JWT токену

```bash
curl -X POST http://localhost:8080/realms/microvolunteer/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=microvolunteer-app" \
  -d "client_secret=microvolunteer-client-secret" \
  -d "username=volunteer" \
  -d "password=volunteer123"
```

### Створення завдання

```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Допомога з покупками",
    "description": "Потрібна допомога з щотижневими покупками",
    "location": "Супермаркет на вул. Головній",
    "categoryIds": [1]
  }'
```

### Пошук завдань

```bash
curl "http://localhost:8081/api/tasks?status=OPEN&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🚀 Розгортання

### Production готовність

- ✅ Health checks endpoints
- ✅ Metrics через Actuator
- ✅ Структуроване логування
- ✅ Graceful shutdown
- ✅ Security headers
- ✅ CORS конфігурація
- ✅ Error handling RFC 7807

### Environment змінні

```bash
# База даних
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/microvolunteer
SPRING_DATASOURCE_USERNAME=microvolunteer_user
SPRING_DATASOURCE_PASSWORD=your_password

# Keycloak
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://keycloak:8080/realms/microvolunteer

# Логування
LOGGING_LEVEL_COM_MICROVOLUNTEER=INFO
```

## 📚 Додаткова документація

- [OpenAPI Specification](http://localhost:8081/v3/api-docs)
- [Swagger UI](http://localhost:8081/swagger-ui.html)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

## 🤝 Внесок

1. Fork проєкт
2. Створіть feature branch (`git checkout -b feature/amazing-feature`)
3. Commit зміни (`git commit -m 'Add amazing feature'`)
4. Push до branch (`git push origin feature/amazing-feature`)
5. Відкрийте Pull Request

## 📄 Ліцензія

Цей проєкт ліцензований під MIT License - дивіться файл [LICENSE](LICENSE) для деталей.

## 🆘 Підтримка

Якщо у вас виникли проблеми або питання:

1. Перевірте [Issues](issues) на GitHub
2. Створіть новий Issue з детальним описом проблеми
3. Надайте логи та інформацію про середовище

---

**MicroVolunteer** - з'єднуємо людей, які потребують допомоги, з тими, хто готовий допомогти! 💙💛
