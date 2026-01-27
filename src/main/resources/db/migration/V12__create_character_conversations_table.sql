-- V12: Store recent character-user conversations

CREATE TABLE character_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    role VARCHAR(16) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_character_conversations_role CHECK (role IN ('USER', 'ASSISTANT'))
);

CREATE INDEX idx_character_conversations_character_created
    ON character_conversations(character_id, created_at DESC);

