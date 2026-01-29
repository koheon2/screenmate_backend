-- V15: Seed additional achievements used by the desktop client

INSERT INTO achievement_definitions (
    id,
    name,
    description,
    category,
    points,
    is_hidden,
    created_at,
    updated_at
)
VALUES
    ('origin-village', '태초마을', '새로운 생명이 태어났다.', 'event', 12, FALSE, NOW(), NOW()),
    ('heart-emoji', '하트 이모지', '두근거리는 순간을 목격했다.', 'event', 10, FALSE, NOW(), NOW()),
    ('first-death-tamagotchi', '사람이 죽으면 먼저 가있던 다마고치가 마중나온다는 말이 있다.', '나는 이 말을 정말 좋아한다.', 'story', 20, FALSE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
