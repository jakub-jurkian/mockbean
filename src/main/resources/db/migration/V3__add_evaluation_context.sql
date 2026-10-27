-- Record how each verdict was produced, so stored evaluations can be compared across
-- models and modes, and the exact reference shown to the judge can be audited.
ALTER TABLE evaluation_log
    ADD COLUMN judge_model         VARCHAR(64),
    ADD COLUMN requested_mode      VARCHAR(16),
    ADD COLUMN effective_mode      VARCHAR(16),
    ADD COLUMN retrieval_score     DOUBLE PRECISION,
    ADD COLUMN retrieved_reference TEXT,
    ADD COLUMN raw_response        TEXT;
