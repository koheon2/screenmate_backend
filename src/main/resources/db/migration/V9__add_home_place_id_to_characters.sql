-- V9: Add home_place_id to characters

ALTER TABLE characters
    ADD COLUMN home_place_id VARCHAR(100);

UPDATE characters
SET home_place_id = 'house1'
WHERE home_place_id IS NULL;

ALTER TABLE characters
    ALTER COLUMN home_place_id SET NOT NULL;

CREATE INDEX idx_characters_home_place_id ON characters(home_place_id);
