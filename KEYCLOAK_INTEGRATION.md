# Keycloak Integration Guide

## Архітектура аутентифікації

Проект використовує **чисту Keycloak архітектуру** без локальних паролів та Basic Auth.

### Основні компоненти

1. **JwtAuthenticationFilter** - обробляє Keycloak JWT токени
2. **JwtService** - парсить Keycloak токени та витягує інформацію
3. **AuthService** - синхронізує користувачів з Keycloak
4. **KeycloakUtils** - утилітарні методи для роботи з поточним користувачем

### Потік автентифікації

1. Користувач автентифікується в Keycloak
2. Keycloak повертає JWT токен
3. Клієнт надсилає токен в заголовку `Authorization: Bearer <token>`
4. `JwtAuthenticationFilter` валідує токен та витягує ролі
5. `AuthService` синхронізує/створює локального користувача

## API Endpoints

### Публічні (не потребують токена)
- `GET /api/categories/**` - категорії
- `GET /api/tasks/list` - список завдань
- `GET /api/tasks/{id}` - деталі завдання
- `GET /api/tasks/recent` - останні завдання

### Захищені (потребують Keycloak токен)
- `POST /api/auth/sync` - синхронізація користувача
- `POST /api/auth/sync-full` - повна синхронізація з JWT
- `GET /api/auth/me` - інформація про поточного користувача
- `POST /api/tasks` - створення завдання
- `POST /api/tasks/{id}/join` - приєднання до завдання

## Конфігурація

### Environment Variables

```bash
# Keycloak
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# JWT (для внутрішніх токенів)
JWT_SECRET=your-secret-key-256-bits
JWT_EXPIRATION=86400000

# Database
DB_USERNAME=microvolunteer
DB_PASSWORD=password
```

### Keycloak Realm Settings

1. **Realm Name**: `microvolunteer`
2. **Client ID**: `microvolunteer-client`
3. **Client Type**: `public` or `confidential`
4. **Valid Redirect URIs**: `http://localhost:3000/*`
5. **Web Origins**: `http://localhost:3000`

### Required Keycloak Roles

- `USER` - базова роль для всіх користувачів
- `ADMIN` - адміністративна роль (опціонально)

## Тестування

### Отримання токена з Keycloak

```bash
# Отримати токен
curl -X POST \
  http://localhost:8080/realms/microvolunteer/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password&client_id=microvolunteer-client&username=testuser&password=testpass'
```

### Використання токена

```bash
# Синхронізація користувача
curl -X POST \
  http://localhost:8081/api/auth/sync \
  -H 'Authorization: Bearer <your-jwt-token>'

# Створення завдання
curl -X POST \
  http://localhost:8081/api/tasks \
  -H 'Authorization: Bearer <your-jwt-token>' \
  -H 'Content-Type: application/json' \
  -d '{"title":"Test Task","description":"Test Description"}'
```

## Структура JWT токена

Очікувана структура Keycloak JWT токена:

```json
{
  "sub": "keycloak-user-id",
  "preferred_username": "username",
  "email": "user@example.com",
  "given_name": "John",
  "family_name": "Doe",
  "realm_access": {
    "roles": ["USER", "ADMIN"]
  },
  "resource_access": {
    "microvolunteer-client": {
      "roles": ["USER"]
    }
  }
}
```

## Troubleshooting

### Помилка "Invalid JWT token"
- Перевірте, чи правильно налаштований Keycloak
- Перевірте, чи токен не застарілий
- Перевірте формат заголовка `Authorization: Bearer <token>`

### Помилка "User not found"
- Викличте `/api/auth/sync` для синхронізації користувача
- Перевірте, чи користувач існує в Keycloak

### Помилка "Access Denied"
- Перевірте ролі користувача в Keycloak
- Перевірте налаштування `@PreAuthorize` в контролерах

## Відмінності від попередньої архітектури

### Видалено
- ❌ `CustomUserDetailsService`
- ❌ Basic Authentication
- ❌ `PasswordEncoder` bean
- ❌ Локальна реєстрація користувачів
- ❌ Локальне зберігання паролів

### Додано
- ✅ Повна інтеграція з Keycloak
- ✅ Автоматична синхронізація користувачів
- ✅ Витягування ролей з Keycloak токенів
- ✅ `KeycloakUtils` для зручної роботи з користувачем
- ✅ Покращена обробка помилок JWT

## Розробка

### Локальний запуск

1. Запустіть Keycloak:
```bash
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev
```

2. Налаштуйте realm та користувачів у Keycloak

3. Запустіть додаток:
```bash
./mvnw spring-boot:run
```

### Docker Compose

```bash
docker-compose up -d
```

Це запустить Keycloak, PostgreSQL та ваш додаток з правильними налаштуваннями.
