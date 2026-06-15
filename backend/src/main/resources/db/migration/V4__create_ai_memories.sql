CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai_memories (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users (id),
    type        VARCHAR(80) NOT NULL,
    content     TEXT        NOT NULL,
    embedding   vector(1536),
    importance  INTEGER     NOT NULL DEFAULT 1,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_memories_user_type ON ai_memories (user_id, type);
