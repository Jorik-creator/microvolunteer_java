-- Додаємо нові колонки до таблиці tasks
ALTER TABLE tasks
    ADD COLUMN deadline TIMESTAMP,
    ADD COLUMN duration INTEGER,
    ADD COLUMN current_volunteers INTEGER DEFAULT 0;

-- Перейменовуємо колонку max_participants на max_volunteers
ALTER TABLE tasks
    RENAME COLUMN max_participants TO max_volunteers;

-- Оновлюємо існуючі записи
UPDATE tasks SET current_volunteers = 0 WHERE current_volunteers IS NULL;
