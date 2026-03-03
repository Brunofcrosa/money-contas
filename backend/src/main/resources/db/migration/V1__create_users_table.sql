CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

COMMENT ON TABLE  users           IS 'Registered users of the personal finance system';
COMMENT ON COLUMN users.id        IS 'Unique user identifier (UUID v4)';
COMMENT ON COLUMN users.email     IS 'User e-mail — used as login credential';
COMMENT ON COLUMN users.password  IS 'BCrypt-hashed password — never stored in plain text';
