CREATE TABLE roles (
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE role_permissions (
    role_id    BIGINT      NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id, permission)
);

CREATE TABLE users (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_id        BIGINT       NOT NULL REFERENCES roles (id),
    email          VARCHAR(255) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_role_id ON users (role_id);

CREATE TABLE verification_tokens (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(100) NOT NULL UNIQUE,
    type       VARCHAR(30)  NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_verification_tokens_user_id ON verification_tokens (user_id);

CREATE TABLE addresses (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    street     VARCHAR(255) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    is_default BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_addresses_user_id ON addresses (user_id);
