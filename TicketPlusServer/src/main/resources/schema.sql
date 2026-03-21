CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    active        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS token_blacklist (
    token_hash VARCHAR(512) PRIMARY KEY,
    expires_at TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires ON token_blacklist (expires_at);
