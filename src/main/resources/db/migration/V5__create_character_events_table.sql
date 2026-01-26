-- V5: Create character_events table for storing character life events

CREATE TABLE character_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_text VARCHAR(1000),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_event_type CHECK (event_type IN ('EVOLUTION', 'DEATH', 'FEEDING', 'PLAYING', 'SPEAKING', 'EMOTION', 'MILESTONE', 'CUSTOM'))
);

CREATE INDEX idx_character_events_character_id ON character_events(character_id);
CREATE INDEX idx_character_events_character_created ON character_events(character_id, created_at DESC);
CREATE INDEX idx_character_events_type ON character_events(character_id, event_type);
