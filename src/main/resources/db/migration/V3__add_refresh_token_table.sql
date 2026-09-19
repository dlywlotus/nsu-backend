CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_rft_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
