INSERT INTO users (keycloak_id, email, first_name, last_name, user_type) VALUES
    ('test-vulnerable-keycloak-id', 'vulnerable@test.com', 'Марія', 'Іваненко', 'AFFECTED_PERSON'),
    ('test-volunteer-keycloak-id', 'volunteer@test.com', 'Олексій', 'Петренко', 'VOLUNTEER'),
    ('test-volunteer2-keycloak-id', 'volunteer2@test.com', 'Анна', 'Сидоренко', 'VOLUNTEER');

INSERT INTO tasks (title, description, category_id, creator_id, location, required_skills, max_participants, status, scheduled_at) VALUES
    ('Допоможіть з покупками', 'Потрібна допомога з покупкою продуктів. Список товарів готовий.', 
     3, 1, 'АТБ, вул. Хрещатик, 20', NULL, 1, 'OPEN', NOW() + INTERVAL '1 day'),
    ('Допомога з налаштуванням технологій', 'Потрібна допомога з налаштуванням нового комп''ютера та встановленням програм.', 
     6, 1, 'м. Київ, вул. Хрещатик, 1 (дома)', 'Знання комп''ютерів, базові IT навички', 1, 'OPEN', NOW() + INTERVAL '2 days'),
    ('Освітній проект для дітей', 'Організація майстер-класу з малювання для дітей з багатодітних сімей.', 
     2, 1, 'Будинок культури, вул. Культурна, 15', 'Досвід роботи з дітьми, художні навички', 3, 'OPEN', NOW() + INTERVAL '3 days'),
    ('Еко-проект: прибирання парку', 'Загальне прибирання міського парку від сміття та листя.', 
     1, 2, 'Центральний парк, вхід з вул. Паркової', NULL, 10, 'OPEN', NOW() + INTERVAL '5 days'),
    ('Спортивний захід для літніх людей', 'Організація ранкової зарядки та легких фізичних вправ для людей похилого віку.', 
     5, 2, 'Спортивний комплекс "Здоров''я", вул. Спортивна, 12', 'Інструктор з фізкультури або досвід роботи з літніми людьми', 2, 'OPEN', NOW() + INTERVAL '7 days');

INSERT INTO participations (task_id, user_id, notes) VALUES
    (1, 2, 'Готовий допомогти з покупками'),
    (2, 3, 'Маю досвід налаштування комп''ютерів'),
    (3, 2, 'Люблю працювати з дітьми');
