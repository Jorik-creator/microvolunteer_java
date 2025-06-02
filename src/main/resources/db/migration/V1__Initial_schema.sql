-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    keycloak_subject VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(15),
    description TEXT,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('VOLUNTEER', 'SENSITIVE', 'ADMIN')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create categories table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create tasks table
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255),
    deadline TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED')),
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Create task_categories junction table for many-to-many relationship
CREATE TABLE task_categories (
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (task_id, category_id)
);

-- Create participations table
CREATE TABLE participations (
    id BIGSERIAL PRIMARY KEY,
    volunteer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    UNIQUE (volunteer_id, task_id, active) -- Prevents duplicate active participations
);

-- Create indexes for better performance
CREATE INDEX idx_users_keycloak_subject ON users(keycloak_subject);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_active ON users(active);

CREATE INDEX idx_categories_name ON categories(name);
CREATE INDEX idx_categories_active ON categories(active);

CREATE INDEX idx_tasks_author_id ON tasks(author_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
CREATE INDEX idx_tasks_deadline ON tasks(deadline);

CREATE INDEX idx_task_categories_task_id ON task_categories(task_id);
CREATE INDEX idx_task_categories_category_id ON task_categories(category_id);

CREATE INDEX idx_participations_volunteer_id ON participations(volunteer_id);
CREATE INDEX idx_participations_task_id ON participations(task_id);
CREATE INDEX idx_participations_active ON participations(active);
CREATE INDEX idx_participations_joined_at ON participations(joined_at);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers to automatically update updated_at columns
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_participations_updated_at
    BEFORE UPDATE ON participations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE users IS 'Table storing user information from Keycloak and additional profile data';
COMMENT ON TABLE categories IS 'Table storing task categories for organization and filtering';
COMMENT ON TABLE tasks IS 'Table storing tasks posted by vulnerable people needing help';
COMMENT ON TABLE task_categories IS 'Junction table for many-to-many relationship between tasks and categories';
COMMENT ON TABLE participations IS 'Table tracking volunteer participation in tasks';

COMMENT ON COLUMN users.keycloak_subject IS 'Unique identifier from Keycloak (sub claim in JWT)';
COMMENT ON COLUMN users.user_type IS 'Type of user: VOLUNTEER (helps others), SENSITIVE (needs help), ADMIN (manages system)';
COMMENT ON COLUMN tasks.status IS 'Current status of the task: OPEN (available), IN_PROGRESS (being worked on), COMPLETED (finished)';
COMMENT ON COLUMN participations.active IS 'Whether the participation is currently active (user has not left the task)';
