-- V3: Create characters table

CREATE TABLE characters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    species VARCHAR(50) NOT NULL,
    personality VARCHAR(500),
    stage_index INTEGER NOT NULL DEFAULT 0 CHECK (stage_index >= 0 AND stage_index <= 3),
    happiness INTEGER NOT NULL DEFAULT 50 CHECK (happiness >= 0 AND happiness <= 100),
    hunger INTEGER NOT NULL DEFAULT 50 CHECK (hunger >= 0 AND hunger <= 100),
    health INTEGER NOT NULL DEFAULT 100 CHECK (health >= 0 AND health <= 100),
    aggression_gauge INTEGER NOT NULL DEFAULT 0 CHECK (aggression_gauge >= 0 AND aggression_gauge <= 100),
    is_alive BOOLEAN NOT NULL DEFAULT TRUE,
    died_at TIMESTAMP WITH TIME ZONE,
    total_play_time_seconds BIGINT NOT NULL DEFAULT 0,
    last_fed_at TIMESTAMP WITH TIME ZONE,
    last_played_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_death_state CHECK (is_alive = TRUE OR died_at IS NOT NULL)
);

CREATE INDEX idx_characters_user_id ON characters(user_id);
CREATE INDEX idx_characters_user_alive ON characters(user_id, is_alive);
