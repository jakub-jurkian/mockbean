CREATE
    EXTENSION IF NOT EXISTS vector;

CREATE TABLE evaluation_log
(
    id              BIGSERIAL PRIMARY KEY,
    question        TEXT,
    user_answer     TEXT,
    score           DOUBLE PRECISION,
    feedback        TEXT,
    strengths       JSONB,
    missed_concepts JSONB,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);