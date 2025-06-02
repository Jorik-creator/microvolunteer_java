-- Insert default categories
INSERT INTO categories (name, description) VALUES
('Shopping & Errands', 'Help with grocery shopping, pharmacy visits, and other errands'),
('Transportation', 'Rides to medical appointments, shopping, or other essential trips'),
('Home Maintenance', 'Minor repairs, cleaning, gardening, and household tasks'),
('Technology Support', 'Help with computers, smartphones, internet, and digital services'),
('Companionship', 'Social visits, conversation, reading together, or recreational activities'),
('Medical Support', 'Assistance with medical appointments, medication management (non-medical)'),
('Pet Care', 'Dog walking, pet sitting, veterinary visits, and pet care assistance'),
('Food & Meals', 'Meal preparation, food delivery, cooking assistance'),
('Education & Learning', 'Tutoring, language practice, skill development, reading assistance'),
('Administrative Help', 'Help with paperwork, applications, government forms, and official tasks');

-- Insert sample users (Note: In production, users would be created through registration after Keycloak auth)
-- These are for testing purposes only

-- Admin user
INSERT INTO users (keycloak_subject, first_name, last_name, email, user_type, description) VALUES
('admin-subject-123', 'System', 'Administrator', 'admin@microvolunteer.com', 'ADMIN', 'System administrator account');

-- Sample sensitive users (people who need help)
INSERT INTO users (keycloak_subject, first_name, last_name, email, phone, user_type, description) VALUES
('sensitive-001', 'Maria', 'Garcia', 'maria.garcia@example.com', '+1234567890', 'SENSITIVE', 'Elderly person living alone, needs help with daily tasks'),
('sensitive-002', 'John', 'Smith', 'john.smith@example.com', '+1234567891', 'SENSITIVE', 'Person with mobility issues, requires assistance with shopping'),
('sensitive-003', 'Anna', 'Johnson', 'anna.johnson@example.com', '+1234567892', 'SENSITIVE', 'Single parent needing help with childcare and errands');

-- Sample volunteer users
INSERT INTO users (keycloak_subject, first_name, last_name, email, phone, user_type, description) VALUES
('volunteer-001', 'David', 'Wilson', 'david.wilson@example.com', '+1234567893', 'VOLUNTEER', 'Retired teacher, available weekdays and weekends'),
('volunteer-002', 'Sarah', 'Brown', 'sarah.brown@example.com', '+1234567894', 'VOLUNTEER', 'Healthcare worker, available evenings and weekends'),
('volunteer-003', 'Michael', 'Davis', 'michael.davis@example.com', '+1234567895', 'VOLUNTEER', 'College student, flexible schedule, owns a car'),
('volunteer-004', 'Emily', 'Taylor', 'emily.taylor@example.com', '+1234567896', 'VOLUNTEER', 'IT professional, specializes in tech support'),
('volunteer-005', 'Robert', 'Anderson', 'robert.anderson@example.com', '+1234567897', 'VOLUNTEER', 'Handyman, available for home maintenance tasks');

-- Insert sample tasks
INSERT INTO tasks (title, description, location, deadline, status, author_id) VALUES
(
    'Weekly Grocery Shopping',
    'I need help with my weekly grocery shopping. I have a prepared list and prefer organic vegetables when possible. I use a walker so carrying heavy bags is difficult for me.',
    'Whole Foods Market, 123 Main Street',
    CURRENT_TIMESTAMP + INTERVAL '3 days',
    'OPEN',
    (SELECT id FROM users WHERE email = 'maria.garcia@example.com')
),
(
    'Computer Setup and Email Help',
    'Just got a new computer and need help setting it up. Also need assistance creating an email account and learning how to send emails to my family.',
    'Home address (will be provided to volunteer)',
    CURRENT_TIMESTAMP + INTERVAL '5 days',
    'OPEN',
    (SELECT id FROM users WHERE email = 'john.smith@example.com')
),
(
    'Ride to Medical Appointment',
    'Need transportation to my monthly cardiology appointment. The clinic is about 15 minutes away. Appointment is scheduled for 2 PM.',
    'Cardiology Clinic, 456 Health Avenue',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    'OPEN',
    (SELECT id FROM users WHERE email = 'anna.johnson@example.com')
),
(
    'Garden Maintenance',
    'My small garden needs attention - weeding, watering, and pruning. I have all the necessary tools. Should take about 2-3 hours.',
    'Backyard garden (residential)',
    CURRENT_TIMESTAMP + INTERVAL '10 days',
    'IN_PROGRESS',
    (SELECT id FROM users WHERE email = 'maria.garcia@example.com')
),
(
    'Medication Pickup',
    'Need someone to pick up my prescription from the pharmacy. I will call ahead and payment is already arranged.',
    'CVS Pharmacy, 789 Pharmacy Lane',
    CURRENT_TIMESTAMP + INTERVAL '2 days',
    'COMPLETED',
    (SELECT id FROM users WHERE email = 'john.smith@example.com')
);

-- Associate tasks with categories
INSERT INTO task_categories (task_id, category_id) VALUES
-- Weekly Grocery Shopping
((SELECT id FROM tasks WHERE title = 'Weekly Grocery Shopping'), 
 (SELECT id FROM categories WHERE name = 'Shopping & Errands')),

-- Computer Setup and Email Help
((SELECT id FROM tasks WHERE title = 'Computer Setup and Email Help'), 
 (SELECT id FROM categories WHERE name = 'Technology Support')),

-- Ride to Medical Appointment
((SELECT id FROM tasks WHERE title = 'Ride to Medical Appointment'), 
 (SELECT id FROM categories WHERE name = 'Transportation')),
((SELECT id FROM tasks WHERE title = 'Ride to Medical Appointment'), 
 (SELECT id FROM categories WHERE name = 'Medical Support')),

-- Garden Maintenance
((SELECT id FROM tasks WHERE title = 'Garden Maintenance'), 
 (SELECT id FROM categories WHERE name = 'Home Maintenance')),

-- Medication Pickup
((SELECT id FROM tasks WHERE title = 'Medication Pickup'), 
 (SELECT id FROM categories WHERE name = 'Shopping & Errands')),
((SELECT id FROM tasks WHERE title = 'Medication Pickup'), 
 (SELECT id FROM categories WHERE name = 'Medical Support'));

-- Insert sample participations
INSERT INTO participations (volunteer_id, task_id, active, notes) VALUES
-- Robert helping with garden maintenance (in progress)
(
    (SELECT id FROM users WHERE email = 'robert.anderson@example.com'),
    (SELECT id FROM tasks WHERE title = 'Garden Maintenance'),
    true,
    'Available Saturday morning, have experience with garden work'
),
-- Sarah completed medication pickup
(
    (SELECT id FROM users WHERE email = 'sarah.brown@example.com'),
    (SELECT id FROM tasks WHERE title = 'Medication Pickup'),
    false,
    'Picked up medication successfully'
);

-- Update completed task
UPDATE tasks 
SET completed_at = CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE title = 'Medication Pickup';

-- Update participation for completed task
UPDATE participations 
SET left_at = CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE task_id = (SELECT id FROM tasks WHERE title = 'Medication Pickup');

-- Add some historical data for better testing
INSERT INTO tasks (title, description, location, status, author_id, created_at, completed_at) VALUES
(
    'Smartphone Tutorial',
    'Needed help learning how to use my new smartphone - making calls, sending texts, and using apps.',
    'Home visit',
    'COMPLETED',
    (SELECT id FROM users WHERE email = 'maria.garcia@example.com'),
    CURRENT_TIMESTAMP - INTERVAL '2 weeks',
    CURRENT_TIMESTAMP - INTERVAL '10 days'
);

-- Historical participation
INSERT INTO participations (volunteer_id, task_id, active, notes, joined_at, left_at) VALUES
(
    (SELECT id FROM users WHERE email = 'emily.taylor@example.com'),
    (SELECT id FROM tasks WHERE title = 'Smartphone Tutorial'),
    false,
    'Great session! User is now comfortable with basic smartphone functions.',
    CURRENT_TIMESTAMP - INTERVAL '2 weeks',
    CURRENT_TIMESTAMP - INTERVAL '10 days'
);

-- Associate historical task with category
INSERT INTO task_categories (task_id, category_id) VALUES
((SELECT id FROM tasks WHERE title = 'Smartphone Tutorial'), 
 (SELECT id FROM categories WHERE name = 'Technology Support'));
