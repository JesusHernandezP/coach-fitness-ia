CREATE TABLE pending_ai_actions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT      NOT NULL REFERENCES users (id),
    conversation_id BIGINT      NOT NULL REFERENCES chat_conversations (id),
    action_type     VARCHAR(80) NOT NULL,
    payload         JSONB       NOT NULL,
    status          VARCHAR(40) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    expires_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_pending_ai_actions_user_conversation
    ON pending_ai_actions (user_id, conversation_id, status);
