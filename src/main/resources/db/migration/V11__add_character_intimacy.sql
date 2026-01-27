-- V11: Add user-character intimacy tracking with daily cap counters

ALTER TABLE characters
    ADD COLUMN intimacy_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN intimacy_daily_date DATE,
    ADD COLUMN intimacy_daily_count INTEGER NOT NULL DEFAULT 0;

