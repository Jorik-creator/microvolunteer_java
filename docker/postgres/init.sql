-- Create additional database for Keycloak if needed
CREATE DATABASE keycloak;
CREATE USER keycloak_user WITH PASSWORD 'keycloak_pass';
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak_user;

-- Grant necessary permissions for microvolunteer user
GRANT ALL PRIVILEGES ON DATABASE microvolunteer TO microvolunteer_user;

-- Set proper encoding
UPDATE pg_database SET datistemplate = FALSE WHERE datname = 'template1';
DROP DATABASE template1;
CREATE DATABASE template1 WITH TEMPLATE = template0 ENCODING = 'UNICODE';
UPDATE pg_database SET datistemplate = TRUE WHERE datname = 'template1';

-- Create extensions if needed
\c microvolunteer;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
