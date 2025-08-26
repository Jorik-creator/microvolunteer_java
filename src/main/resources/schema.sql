-- Налаштування кодування для сесії
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- Створення простого тесту
CREATE TABLE IF NOT EXISTS test_encoding (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Видалення тестової таблиці
DROP TABLE IF EXISTS test_encoding;