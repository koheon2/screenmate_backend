-- V10: Character lineage edges for family tree graphs

CREATE TABLE character_lineage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    child_character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    parent_a_character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    parent_b_character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_lineage_distinct_parents CHECK (parent_a_character_id <> parent_b_character_id),
    CONSTRAINT chk_lineage_child_not_parent_a CHECK (child_character_id <> parent_a_character_id),
    CONSTRAINT chk_lineage_child_not_parent_b CHECK (child_character_id <> parent_b_character_id),
    CONSTRAINT uq_lineage_child UNIQUE (child_character_id)
);

CREATE INDEX idx_lineage_child ON character_lineage(child_character_id);
CREATE INDEX idx_lineage_parent_a ON character_lineage(parent_a_character_id);
CREATE INDEX idx_lineage_parent_b ON character_lineage(parent_b_character_id);
