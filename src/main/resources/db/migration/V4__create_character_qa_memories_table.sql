-- V4: Create character_qa_memories table for storing QA data with optimistic locking

CREATE TABLE character_qa_memories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    character_id UUID NOT NULL UNIQUE REFERENCES characters(id) ON DELETE CASCADE,
    qa_data JSONB NOT NULL DEFAULT '{}',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_character_qa_memories_character_id ON character_qa_memories(character_id);
CREATE INDEX idx_character_qa_memories_qa_data ON character_qa_memories USING GIN (qa_data);
