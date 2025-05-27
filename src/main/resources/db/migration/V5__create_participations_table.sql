CREATE TABLE participations (
    id SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    feedback TEXT,
    UNIQUE(task_id, user_id)
);

CREATE INDEX idx_participations_task ON participations(task_id);
CREATE INDEX idx_participations_user ON participations(user_id);