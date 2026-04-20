CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL
);

CREATE TABLE IF NOT EXISTS metabolic_profiles (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT      NOT NULL UNIQUE REFERENCES users (id),
    age                  INTEGER,
    sex                  VARCHAR(50),
    height_cm            DOUBLE PRECISION,
    current_weight_kg    DOUBLE PRECISION,
    activity_level       VARCHAR(50),
    weekly_exercise_days INTEGER,
    exercise_type        VARCHAR(255),
    exercise_minutes     INTEGER,
    daily_steps          INTEGER,
    diet_type            VARCHAR(50),
    goal                 VARCHAR(50),
    updated_at           TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS nutrition_targets (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT           NOT NULL UNIQUE REFERENCES users (id),
    calories      DOUBLE PRECISION,
    protein_g     DOUBLE PRECISION,
    carbs_g       DOUBLE PRECISION,
    fat_g         DOUBLE PRECISION,
    calculated_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS weight_logs (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT           NOT NULL REFERENCES users (id),
    weight_kg  DOUBLE PRECISION NOT NULL,
    logged_at  TIMESTAMPTZ      NOT NULL
);

CREATE TABLE IF NOT EXISTS activity_logs (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT  NOT NULL REFERENCES users (id),
    date             DATE    NOT NULL,
    steps            INTEGER,
    calories_burned  INTEGER,
    notes            VARCHAR(255),
    UNIQUE (user_id, date)
);

CREATE TABLE IF NOT EXISTS chat_conversations (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    title      VARCHAR(255),
    created_at TIMESTAMPTZ  NOT NULL
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT       NOT NULL REFERENCES chat_conversations (id),
    role            VARCHAR(50)  NOT NULL,
    content         TEXT         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL
);
