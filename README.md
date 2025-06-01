# 🤝 Microvolunteer Platform

Платформа для з'єднання волонтерів та людей, які потребують допомоги.

## 🚀 Швидкий старт

### 1. Запуск з H2 базою (для розробки)

```bash
# Запуск з dev профілем
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Або через JAR
./mvnw clean package
java -jar target/microvolunteer-spring-1.0.0.jar --spring.profiles.active=dev
```

### 2. Доступ до Swagger UI

Після запуску приложения:

**🌐 Swagger UI:** http://localhost:8081/swagger-ui.html

**📚 API Documentation:** http://localhost:8081/v3/api-docs

**💾 H2 Console (dev режим):** http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:microvolunteer_dev`
- Username: `sa`
- Password: (пустой)

### 3. Тестовые endpoints

**Проверка здоровья API:**
```bash
curl http://localhost:8081/api/health
```

**Информация о API:**
```bash
curl http://localhost:8081/api/info
```

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

### Разработка (только API с H2)
```bash
# Собрать образ
docker build -t microvolunteer-api .

# Запустить контейнер
docker run -p 8081:8081 -e SPRING_PROFILES_ACTIVE=dev microvolunteer-api
```

### Продакшн (полная инфраструктура)
```bash
# Запуск всех сервисов
docker-compose up -d

# Проверка логов
docker-compose logs -f microvolunteer-springboot
```

## 🗄️ База данных

### Миграции Flyway
Миграции находятся в `src/main/resources/db/migration/`:
- `V1__Initial_schema.sql` - Начальная схема
- `V2__insert_test_data.sql` - Тестовые данные

### Тестовые данные
При запуске создаются:
- **Категории:** Екологія, Освіта, Соціальна допомога и др.
- **Пользователи:** Тестовые волонтеры и организаторы
- **Задания:** Примеры заданий с разными статусами

## 🔧 Конфигурация

### Профили Spring
- **dev** - H2 база, подробные логи, Swagger включен
- **docker** - PostgreSQL, оптимизированные настройки
- **test** - H2 in-memory для тестов

### Переменные окружения

#### Для Docker:
```env
DB_HOST=postgres
DB_PORT=5432
DB_USERNAME=microvolunteer
DB_PASSWORD=secure_password
KEYCLOAK_URL=http://keycloak:8080
JWT_SECRET=your-super-secret-jwt-key
```

#### Для разработки:
```env
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8081
LOG_LEVEL=DEBUG
```

## 🧪 Тестирование

```bash
# Запуск всех тестов
./mvnw test

# Только unit тесты
./mvnw test -Dtest="*Test"

# Только интеграционные тесты
./mvnw test -Dtest="*IntegrationTest"
```

## 📚 Swagger UI возможности

### Авторизация
1. Откройте http://localhost:8081/swagger-ui.html
2. Нажмите кнопку "Authorize" 🔓
3. Введите JWT токен в формате: `Bearer your-jwt-token`

### Тестирование API
- ✅ Все endpoints доступны для тестирования
- 📝 Подробные описания и примеры
- 🔄 Возможность выполнить запросы прямо из UI
- 📊 Просмотр схем данных и моделей

### Примеры запросов
В Swagger UI доступны готовые примеры для:
- Создания заданий
- Регистрации пользователей  
- Поиска с фильтрами
- Управления участием

## 🏗️ Архитектура

```
src/main/java/com/microvolunteer/
├── controller/     # REST контроллеры
├── service/        # Бизнес-логика
├── repository/     # Доступ к данным
├── entity/         # JPA сущности
├── dto/           # Data Transfer Objects
├── mapper/        # MapStruct маппинг
├── config/        # Конфигурация
├── exception/     # Обработка ошибок
└── enums/         # Перечисления
```

## 🛠️ Технологии

- **Spring Boot 3.4.4** - Основной фреймворк
- **Spring Security** - Безопасность и JWT
- **Spring Data JPA** - ORM и работа с БД
- **PostgreSQL** - Основная БД (продакшн)
- **H2** - БД для разработки и тестов
- **Flyway** - Миграции БД
- **Keycloak** - Управление пользователями
- **Swagger/OpenAPI 3** - Документация API
- **MapStruct** - Маппинг объектов
- **Docker** - Контейнеризация
- **Maven** - Сборка проекта

## ❓ Решение проблем

### Swagger UI не открывается
1. Проверьте, что приложение запущено: http://localhost:8081/api/health
2. Попробуйте другие URL:
   - http://localhost:8081/swagger-ui/
   - http://localhost:8081/swagger-ui/index.html
3. Проверьте логи на ошибки

### Проблемы с базой данных
```bash
# Проверить подключение к H2 (dev)
curl http://localhost:8081/h2-console

# Очистить данные и перезапустить
./mvnw clean spring-boot:run -Dspring-boot.run.profiles=dev
```

### Проблемы с Docker
```bash
# Проверить статус контейнеров
docker-compose ps

# Пересобрать и запустить
docker-compose down
docker-compose up --build -d
```

## 🤝 Участие в разработке

1. Fork репозитория
2. Создайте ветку для изменений
3. Добавьте тесты для новой функциональности
4. Запустите тесты: `./mvnw test`
5. Создайте Pull Request

---

**🌟 Готово! Swagger UI доступен по адресу:** http://localhost:8081/swagger-ui.html
