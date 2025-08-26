-- Тестові користувачі для мікро-волонтерства
-- Паролі закодовані (пароль: password123) дуже секретно сховав...
INSERT INTO users (username, email, password, first_name, last_name, user_type, phone, bio, address, is_active, date_joined, last_updated) VALUES
-- Адміністратори
('admin', 'admin@microvolunteering.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Адмін', 'Система', 'ADMIN', '+380501234567', 'Системний адміністратор платформи мікро-волонтерства', 'Київ, Україна', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Волонтери
('maryna_volunteer', 'maryna@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Марина', 'Петренко', 'VOLUNTEER', '+380671234567', 'Люблю допомагати людям, особливо літнім людям. Маю досвід догляду за тваринами.', 'Київ, вул. Хрещатик, 1', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('oleksandr_helper', 'oleksandr@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Олександр', 'Коваленко', 'VOLUNTEER', '+380632345678', 'Студент IT, можу допомогти з комп''ютерами та технологіями. Вільний вечорами та вихідними.', 'Київ, вул. Лесі Українки, 26', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('oksana_eco', 'oksana@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Оксана', 'Зеленська', 'VOLUNTEER', '+380933456789', 'Активістка екологічного руху. Організовую прибирання парків та екологічні акції.', 'Київ, вул. Володимирська, 45', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('dmytro_teacher', 'dmytro@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Дмитро', 'Іваненко', 'VOLUNTEER', '+380504567890', 'Вчитель математики, можу допомогти з навчанням дітей. Досвід роботи 10 років.', 'Київ, вул. Саксаганського, 33', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('anna_sport', 'anna@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Анна', 'Спортивна', 'VOLUNTEER', '+380685678901', 'Фітнес-тренер, організовую безкоштовні тренування для дітей та літніх людей.', 'Київ, вул. Спортивна, 12', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Люди, які потребують допомоги
('maria_babusya', 'maria@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Марія', 'Іванівна', 'VULNERABLE', '+380736789012', 'Пенсіонерка, потребую допомоги з покупками та по дому. Живу одна.', 'Київ, вул. Перемоги, 67, кв. 15', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('vasyl_pensioner', 'vasyl@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Василь', 'Петрович', 'VULNERABLE', '+380787890123', 'Пенсіонер, маю проблеми з мобільністю. Потребую допомоги з походами до лікаря.', 'Київ, вул. Мирна, 23, кв. 8', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('tetyana_mama', 'tetyana@gmail.com', '$2a$10$CwTycUXWue0Tbp9z.L1/0O8lSH8wB8wU8wRU8VR8FR8TR8FR8FR8F', 'Тетяна', 'Мамочка', 'VULNERABLE', '+380638901234', 'Мама трьох дітей, потребую допомоги з доглядом за дітьми та навчанням.', 'Київ, вул. Сімейна, 45, кв. 22', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('igor_student', 'igor@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Ігор', 'Студентський', 'VULNERABLE', '+380689012345', 'Студент з малозабезпеченої сім''ї, потребую допомоги з навчанням та підготовкою до іспитів.', 'Київ, вул. Університетська, 12, кв. 5', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('lyubov_shelter', 'lyubov@gmail.com', '$2a$10$50aM.pGWaenm8QjPlp3HKuFvD/ekrOwBbetao8lsgbeYEY7zH3J1u', 'Любов', 'Доглядачка', 'VULNERABLE', '+380730123456', 'Працюю в притулку для тварин, потребуємо допомоги волонтерів для догляду за тваринами.', 'Київ, вул. Тваринна, 88', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);