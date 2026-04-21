CREATE
EXTENSION IF NOT EXISTS vector;

CREATE TABLE evaluation_log
(
    id            BIGSERIAL PRIMARY KEY,
    question_id   BIGINT        NOT NULL,
    user_answer   TEXT          NOT NULL,
    model_name    VARCHAR(50)   NOT NULL,
    score         NUMERIC(3, 2) NOT NULL,
    feedback_json JSONB         NOT NULL,
    created_at    TIMESTAMPTZ DEFAULT NOW()
);