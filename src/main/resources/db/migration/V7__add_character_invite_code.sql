-- V7: Add invite code to characters

ALTER TABLE characters
    ADD COLUMN invite_code VARCHAR(12);

UPDATE characters
SET invite_code = SUBSTRING(REPLACE(gen_random_uuid()::text, '-', '') FROM 1 FOR 8)
WHERE invite_code IS NULL;

ALTER TABLE characters
    ALTER COLUMN invite_code SET NOT NULL;

CREATE UNIQUE INDEX uq_characters_invite_code ON characters(invite_code);
