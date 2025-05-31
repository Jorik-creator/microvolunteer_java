CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(150) UNIQUE NOT NULL,
    email VARCHAR(254) UNIQUE NOT NULL,
    password VARCHAR(128) NOT NULL,
    first_name VARCHAR(150),
    last_name VARCHAR(150),
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('VOLUNTEER', 'VULNERABLE')),
    phone VARCHAR(20),
    bio TEXT,
    address VARCHAR(255),
    profile_image VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    date_joined TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    keycloak_id VARCHAR(255) UNIQUE
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_user_type ON users(user_type);