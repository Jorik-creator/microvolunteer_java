# Microvolunteer - Платформа для волонтерських завдань

Платформа для з'єднання вразливих людей з волонтерами для виконання різноманітних завдань.

## ✅ Стан проекту

**ВИПРАВЛЕНІ ПРОБЛЕМИ:**
- ✅ SecurityConfig - додано правильні endpoint-и 
- ✅ TaskService - виправлено логіку підрахунку учасників
- ✅ DTO та Mappers - виправлено назви полів
- ✅ Repository - замінено MySQL функції на PostgreSQL
- ✅ Docker - оновлено конфігурацію з правильними назвами контейнерів
- ✅ Flyway - оновлено до версії 11.0.1 для підтримки PostgreSQL 16
- ✅ Залежності - всі version conflicts виправлені

## Технології

- **Backend**: Spring Boot 3.4, Java 17, PostgreSQL 16, Flyway 11
- **Безпека**: Keycloak 26.0.7, JWT
- **Документація**: Swagger/OpenAPI 2.7.0
- **Контейнеризація**: Docker, Docker Compose
- **Збірка**: Maven
- **Тестування**: JUnit 5, Testcontainers

## 🚀 Швидкий старт

### Перевірка працездатності

```bash
# Швидка перевірка (Windows)
quick-test.bat

# Швидка перевірка (Linux/Mac)
chmod +x quick-test.sh
./quick-test.sh
```

### Розробка

1. **Запуск інфраструктури**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

2. **Перевірка статусу**
   ```bash
   docker ps
   # Всі контейнери повинні бути в статусі "Up"
   ```

3. **Запуск додатку**
   ```bash
   # Windows
   run-dev.bat
   
   # Linux/Mac
   chmod +x run-dev.sh
   ./run-dev.sh
   ```

### Продакшен

1. **Налаштування змінних оточення**
   ```bash
   cp .env.example .env
   # Відредагуйте .env файл зі своїми налаштуваннями
   ```

2. **Запуск всього стеку**
   ```bash
   docker-compose up -d
   ```

## 🌐 Доступ до сервісів

### Розробка
- **Додаток**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Keycloak Admin**: http://localhost:8082 (admin/admin)
- **PostgreSQL**: localhost:5433 (microvolunteer/password)

### Продакшен
- **Додаток**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **Keycloak Admin**: http://localhost:8080
- **PostgreSQL**: localhost:5432

## 📚 API Документація

API документація доступна за адресою `/swagger-ui.html` після запуску додатку.

### Основні endpoint-и

#### Аутентифікація
- `POST /api/auth/register` - Реєстрація
- `POST /api/auth/sync` - Синхронізація з Keycloak
- `POST /api/auth/token` - Генерація JWT токена ✅

#### Завдання
- `GET /api/tasks/list` - Список завдань
- `GET /api/tasks/{id}` - Деталі завдання
- `POST /api/tasks` - Створення завдання
- `PUT /api/tasks/{id}` - Оновлення завдання
- `POST /api/tasks/{id}/join` - Приєднання до завдання ✅
- `POST /api/tasks/{id}/leave` - Відмова від участі ✅
- `POST /api/tasks/{id}/complete` - Завершення завдання

#### Категорії
- `GET /api/categories` - Список категорій
- `POST /api/categories` - Створення категорії

## 🧪 Тестування

```bash
# Unit тести
./mvnw test

# Компіляція без тестів
./mvnw clean compile -DskipTests

# Повна збірка
./mvnw clean install
```

## 🗄️ База даних

Проект використовує Flyway для міграцій. Файли міграцій знаходяться в `src/main/resources/db/migration/`.

### Основні таблиці:
- `users` - Користувачі
- `tasks` - Завдання ✅
- `categories` - Категорії
- `participations` - Участь у завданнях ✅
- `task_images` - Зображення завдань

## ⚙️ Конфігурація

### Профілі Spring
- `default` - Локальна розробка
- `dev` - Розробка з зовнішніми сервісами ✅
- `docker` - Контейнеризоване середовище ✅
- `test` - Тестування

### Змінні оточення
Див. `.env.example` для повного списку доступних змінних.

## 🐳 Docker

### Контейнери для розробки:
- `microvolunteer-postgres-dev` - PostgreSQL 16
- `microvolunteer-keycloak-dev` - Keycloak 26.0.7  
- `microvolunteer-keycloak-postgres-dev` - БД для Keycloak
- `microvolunteer-redis-dev` - Redis для кешування

### Контейнери для продакшену:
- `microvolunteer-springboot` - Spring Boot додаток ✅
- `microvolunteer-postgres` - PostgreSQL
- `microvolunteer-keycloak` - Keycloak
- `microvolunteer-keycloak-postgres` - БД для Keycloak
- `microvolunteer-redis` - Redis

## 🔧 Розробка

### Структура проекту
```
src/
├── main/
│   ├── java/com/microvolunteer/
│   │   ├── config/          # Конфігурація Spring ✅
│   │   ├── controller/      # REST контролери ✅
│   │   ├── dto/            # DTO класи ✅
│   │   ├── entity/         # JPA сутності ✅
│   │   ├── enums/          # Енумерації
│   │   ├── exception/      # Обробка помилок ✅
│   │   ├── mapper/         # MapStruct мапери ✅
│   │   ├── repository/     # Spring Data репозиторії ✅
│   │   └── service/        # Бізнес логіка ✅
│   └── resources/
│       ├── db/migration/   # Flyway міграції ✅
│       └── application*.yml # Конфігурація ✅
└── test/                   # Тести
```

### Виправлені проблеми
1. **TaskService** - правильний підрахунок `currentVolunteers`
2. **Repository** - PostgreSQL-сумісні SQL запити
3. **Security** - правильна конфігурація endpoint-ів
4. **Docker** - унікальні назви контейнерів
5. **DTO** - консистентні назви полів (`maxVolunteers`)

## 📊 Моніторинг

Додаток надає Actuator endpoints для моніторингу:
- `/actuator/health` - Статус здоров'я
- `/actuator/info` - Інформація про додаток
- `/actuator/metrics` - Метрики

## ⚠️ Важливі нотатки

1. **Keycloak** налаштований для розробки з admin/admin
2. **PostgreSQL** використовує порт 5433 для розробки
3. **JWT токени** мають термін дії 24 години
4. **Flyway** використовує версію 11.0.1 для підтримки PostgreSQL 16

## 🚧 Наступні кроки

1. ✅ Підготовка бази даних
2. ✅ Spring Boot Application  
3. ✅ Написання контролерів
4. ✅ Написання сервісів
5. ✅ Підключення Keycloak
6. ✅ Підключення Swagger UI
7. ✅ Збірка в Docker контейнери
8. 🔄 **Написання Unit тестів**
9. 🔄 **Написання інтеграційних автотестів**

## 💡 Поради для розробки

- Використовуйте `quick-test.bat/sh` для швидкої перевірки
- Перевіряйте логи контейнерів: `docker logs microvolunteer-postgres-dev`
- Використовуйте Swagger UI для тестування API
- Keycloak admin panel доступний на http://localhost:8082

## 📄 Ліцензія

MIT License
