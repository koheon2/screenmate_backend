-- V8: User achievements and discovered places

CREATE TABLE achievement_definitions (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50),
    points INTEGER NOT NULL DEFAULT 0,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_id VARCHAR(100) NOT NULL REFERENCES achievement_definitions(id) ON DELETE RESTRICT,
    progress INTEGER NOT NULL DEFAULT 0,
    unlocked_at TIMESTAMP WITH TIME ZONE,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_id)
);

CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_unlocked_at ON user_achievements(unlocked_at);

CREATE TABLE place_definitions (
    id VARCHAR(100) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    region VARCHAR(100),
    rarity VARCHAR(50),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_discovered_places (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    place_id VARCHAR(100) NOT NULL REFERENCES place_definitions(id) ON DELETE RESTRICT,
    discovered_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discovered_by_character_id UUID REFERENCES characters(id) ON DELETE SET NULL,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_place UNIQUE (user_id, place_id)
);

CREATE INDEX idx_user_places_user_id ON user_discovered_places(user_id);
CREATE INDEX idx_user_places_discovered_at ON user_discovered_places(discovered_at);
