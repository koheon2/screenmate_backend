-- V6: Create friend request, friendship, and friend message tables

CREATE TABLE character_friend_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    receiver_character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    message VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_friend_request_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELED')),
    CONSTRAINT chk_friend_request_self CHECK (requester_character_id <> receiver_character_id)
);

CREATE INDEX idx_friend_requests_requester ON character_friend_requests(requester_character_id);
CREATE INDEX idx_friend_requests_receiver ON character_friend_requests(receiver_character_id);
CREATE INDEX idx_friend_requests_status ON character_friend_requests(status);

CREATE TABLE character_friendships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    character_a_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    character_b_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    intimacy INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_friendship_self CHECK (character_a_id <> character_b_id),
    CONSTRAINT uq_friendship_pair UNIQUE (character_a_id, character_b_id)
);

CREATE INDEX idx_friendships_character_a ON character_friendships(character_a_id);
CREATE INDEX idx_friendships_character_b ON character_friendships(character_b_id);

CREATE TABLE character_friend_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    friendship_id UUID NOT NULL REFERENCES character_friendships(id) ON DELETE CASCADE,
    sender_character_id UUID NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    message_text VARCHAR(200),
    emote_id VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_friend_message_content CHECK (message_text IS NOT NULL OR emote_id IS NOT NULL)
);

CREATE INDEX idx_friend_messages_friendship ON character_friend_messages(friendship_id, created_at DESC);
