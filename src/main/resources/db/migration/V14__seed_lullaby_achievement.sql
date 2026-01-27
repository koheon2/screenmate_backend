-- V14: Seed lullaby achievement used by the desktop client

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
    ('lullaby-baby', '자장자장 우리아가', '요람에서 우리 아가를 지켜봤다.', 'care', 8, FALSE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
