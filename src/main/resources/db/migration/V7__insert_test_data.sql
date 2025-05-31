-- Створюємо тестових користувачів
INSERT INTO users (username, email, password, first_name, last_name, user_type, phone, bio, address, is_active, keycloak_id) VALUES
    ('test_vulnerable', 'vulnerable@test.com', '$2a$10$dummy.password.hash', 'Марія', 'Іваненко', 'VULNERABLE', '+380501234567', 'Потребую допомоги з покупками', 'м. Київ, вул. Хрещатик, 1', true, 'test-user-keycloak-id'),
    ('test_volunteer', 'volunteer@test.com', '$2a$10$dummy.password.hash', 'Олексій', 'Петренко', 'VOLUNTEER', '+380509876543', 'Готовий допомагати', 'м. Київ, вул. Лесі Українки, 10', true, 'test-volunteer-keycloak-id'),
    ('test_volunteer2', 'volunteer2@test.com', '$2a$10$dummy.password.hash', 'Анна', 'Сидоренко', 'VOLUNTEER', '+380507777777', 'Люблю допомагати людям', 'м. Київ, вул. Бандери, 15', true, 'test-volunteer2-keycloak-id');

-- Створюємо тестові завдання
INSERT INTO tasks (title, description, category_id, creator_id, location, start_date, end_date, max_volunteers, current_volunteers, status) VALUES
    ('Допоможіть з покупками', 'Потрібна допомога з покупкою продуктів. Список товарів готовий.', 1, 1, 'АТБ, вул. Хрещатик, 20', NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 2 hours', 1, 0, 'OPEN'),
    ('Супровід до лікаря', 'Потрібен супровід до поліклініки на обстеження.', 5, 1, 'Поліклініка №1, вул. Медична, 5', NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days 3 hours', 1, 0, 'OPEN'),
    ('Допомога з переїздом', 'Потрібна допомога з перенесенням речей на новий адрес.', 2, 1, 'вул. Старий адрес, 10 -> вул. Новий адрес, 25', NOW() + INTERVAL '3 days', NOW() + INTERVAL '3 days 4 hours', 2, 0, 'OPEN'),
    ('Налаштування комп''ютера', 'Потрібна допомога з налаштуванням нового комп''ютера та встановленням програм.', 3, 1, 'м. Київ, вул. Хрещатик, 1 (дома)', NOW() + INTERVAL '4 days', NOW() + INTERVAL '4 days 2 hours', 1, 0, 'OPEN'),
    ('Прибирання в квартирі', 'Потрібна допомога з генеральним прибиранням.', 4, 1, 'м. Київ, вул. Хрещатик, 1 (дома)', NOW() + INTERVAL '5 days', NOW() + INTERVAL '5 days 3 hours', 2, 0, 'OPEN');
