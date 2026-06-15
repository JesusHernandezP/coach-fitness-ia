CREATE TABLE food_logs (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT           NOT NULL REFERENCES users (id),
    date         DATE             NOT NULL,
    meal_type    VARCHAR(50)      NOT NULL,
    description  TEXT             NOT NULL,
    calories     DOUBLE PRECISION NOT NULL,
    protein_g    DOUBLE PRECISION,
    carbs_g      DOUBLE PRECISION,
    fat_g        DOUBLE PRECISION,
    source       VARCHAR(50)      NOT NULL,
    confidence   DOUBLE PRECISION,
    created_at   TIMESTAMPTZ      NOT NULL,
    updated_at   TIMESTAMPTZ
);

CREATE INDEX idx_food_logs_user_date ON food_logs (user_id, date);
