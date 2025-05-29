# Troubleshooting - Microvolunteer

## 🛠️ Найчастіші проблеми та рішення

### 1. Проблеми з базою даних

#### "Connection to localhost:5433 refused" або "PostgreSQL connection failed"

**Причина:** PostgreSQL контейнер не запущений або недоступний

**Рішення:**
```bash
# Перевірити статус контейнерів
docker ps

# Перезапустити PostgreSQL
docker-compose -f docker-compose.dev.yml restart microvolunteer-postgres-dev

# Перевірити логи
docker logs microvolunteer-postgres-dev

# Якщо база даних не існує:
docker exec -it microvolunteer-postgres-dev psql -U microvolunteer -c "\l"
```

#### "Flyway validation failed" або "Unsupported Database: PostgreSQL X.X"

**Причина:** Несумісність версій Flyway та PostgreSQL

**Рішення:**
1. Переконайтеся що використовується PostgreSQL 16 (не 17)
2. Flyway версія 11.0.1+ в pom.xml
3. Очистіть міграції якщо потрібно:
```bash
docker exec -it microvolunteer-postgres-dev psql -U microvolunteer -d microvolunteer_dev -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

### 2. Проблеми з Keycloak

#### "Keycloak is starting" або "Connection refused to Keycloak"

**Причина:** Keycloak довго запускається або не може підключитися до БД

**Рішення:**
```bash
# Перевірити логи Keycloak
docker logs microvolunteer-keycloak-dev

# Створити базу для Keycloak якщо не існує
docker exec -it microvolunteer-postgres-dev psql -U microvolunteer -c "CREATE DATABASE keycloak;"

# Перезапустити Keycloak
docker-compose -f docker-compose.dev.yml restart microvolunteer-keycloak-dev
```

#### "Admin console недоступний"

**Рішення:**
- Keycloak доступний на http://localhost:8082 (не 8080 в dev режимі)
- Логін: admin, Пароль: admin
- Зачекайте 2-3 хвилини після запуску

### 3. Проблеми компіляції

#### "Package org.mapstruct does not exist"

**Причина:** MapStruct annotation processor не налаштований

**Рішення:**
```bash
# Очистити та перекомпілювати
mvnw.cmd clean compile

# Якщо не допомагає - видалити target та перезапустити IDE
rmdir /s target
```

#### "Cannot find symbol: class TaskMapper"

**Причина:** MapStruct не згенерував mappers

**Рішення:**
1. Перевірити наявність `@Mapper(componentModel = "spring")` 
2. Перекомпілювати проект
3. Перезапустити IDE

### 4. Проблеми з Docker

#### "Port already in use" або "bind: address already in use"

**Причина:** Порти зайняті іншими сервісами

**Рішення:**
```bash
# Знайти процес що використовує порт
netstat -ano | findstr :8081
netstat -ano | findstr :5433

# Вбити процес (замінити PID)
taskkill /PID 1234 /F

# Або змінити порти в .env файлі
```

#### "No such file or directory" при запуску sh скриптів

**Причина:** Неправильні line endings (CRLF замість LF)

**Рішення:**
```bash
# Конвертувати line endings
sed -i 's/\r$//' run-dev.sh
chmod +x run-dev.sh

# Або використовувати bat файли на Windows
```

### 5. Проблеми з Spring Boot

#### "Failed to start application" - JwtAuthenticationFilter

**Причина:** Circular dependency або неправильна конфігурація Security

**Рішення:**
1. Перевірити що всі @Component класи мають правильні анотації
2. Перевірити SecurityConfig на циклічні залежності

#### "No qualifying bean of type" для Repository/Service

**Причина:** ComponentScan не знаходить класи

**Рішення:**
1. Перевірити package structure (всі класи під com.microvolunteer)
2. Додати @Repository/@Service анотації
3. Перевірити @SpringBootApplication конфігурацію

### 6. Проблеми з тестами

#### "MockBean is deprecated"

**Причина:** Spring Boot 3.4 deprecated @MockBean

**Рішення:**
```java
// Замість
@MockBean
private UserService userService;

// Використовувати
@MockitoBean  
private UserService userService;
```

#### "TestContainers failed to start"

**Причина:** Docker не запущений або немає прав

**Рішення:**
1. Запустити Docker Desktop
2. Перевірити права на /var/run/docker.sock (Linux)

### 7. Проблеми з JWT

#### "JWT token validation error"

**Причина:** Неправильний secret key або expired token

**Рішення:**
1. Перевірити app.jwt.secret в application.yml
2. Переконатися що secret >= 256 bits
3. Перевірити термін дії токена

### 8. Швидкі команди для діагностики

```bash
# Повна перевірка системи
quick-test.bat  # Windows
./quick-test.sh # Linux/Mac

# Перевірка портів
netstat -tulpn | grep :808  # Linux
netstat -ano | findstr :808 # Windows

# Перевірка Docker
docker system df
docker system prune -f

# Перевірка логів всіх контейнерів
docker-compose -f docker-compose.dev.yml logs

# Перезапуск всього стеку
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d
```

### 9. Якщо нічого не допомагає

1. **Повна очистка:**
```bash
# Зупинити все
docker-compose -f docker-compose.dev.yml down -v

# Видалити volumes 
docker volume prune -f

# Очистити Maven
mvnw.cmd clean

# Перезапустити Docker Desktop
```

2. **Перевірити environment:**
```bash
# Java версія
java -version  # повинна бути 17+

# Maven версія  
mvnw.cmd -version

# Docker версія
docker --version
docker-compose --version
```

3. **Логи для підтримки:**
```bash
# Зберегти логи всіх сервісів
docker-compose -f docker-compose.dev.yml logs > debug-logs.txt

# Maven debug лог
mvnw.cmd clean compile -X > maven-debug.txt 2>&1
```

### 📞 Отримання допомоги

1. Перевірити цей файл troubleshooting
2. Запустити `quick-test` для діагностики
3. Зберегти логи командами вище
4. Створити issue з логами та описом проблеми

---

**💡 Порада:** Завжди запускайте `quick-test` перед початком розробки!
