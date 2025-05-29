-- Ініціалізація PostgreSQL для Microvolunteer проекту
-- Цей скрипт виконується при першому запуску контейнера

-- Створення бази даних для Keycloak
SELECT 'CREATE DATABASE keycloak OWNER microvolunteer'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak')\gexec

-- Створення схеми для Keycloak в основній базі (резерв)
CREATE SCHEMA IF NOT EXISTS keycloak;

-- Надання прав користувачу microvolunteer
GRANT ALL PRIVILEGES ON DATABASE microvolunteer TO microvolunteer;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO microvolunteer;
GRANT ALL PRIVILEGES ON SCHEMA keycloak TO microvolunteer;
GRANT ALL PRIVILEGES ON SCHEMA public TO microvolunteer;

-- Налаштування для оптимізації роботи
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET pg_stat_statements.track = 'all';
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_min_duration_statement = 1000;

-- Створення індексів для покращення продуктивності (якщо потрібно)
-- Додаткові налаштування можна додати тут

-- Вивід інформації про успішну ініціалізацію
\echo 'PostgreSQL successfully initialized for Microvolunteer project'
\echo 'Databases: microvolunteer (main), keycloak (auth)'
\echo 'User: microvolunteer with full privileges'