-- V13: Seed custom achievements used by the desktop client

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
    ('payday', '페이데이', '은행에서 대담한 일을 벌였다.', 'event', 10, FALSE, NOW(), NOW()),
    ('theft', '절도', '경찰서 앞에서 반성(?)하는 시간을 가졌다.', 'event', 10, FALSE, NOW(), NOW()),
    ('iced-americano', '얼죽아', '추운 날에도 아이스 아메리카노를 마셨다.', 'lifestyle', 8, FALSE, NOW(), NOW()),
    ('truancy', '땡떙이', '학교에 갔지만 딴생각만 했다.', 'school', 6, FALSE, NOW(), NOW()),
    ('no-peek', '엿보지 마세요!', '화장실에서 방해받지 않는 시간을 보냈다.', 'event', 6, FALSE, NOW(), NOW()),
    ('old-friend', '죽마고우', '오래된 친구와 공원에서 시간을 보냈다.', 'social', 12, FALSE, NOW(), NOW()),
    ('medicine', '병주고 약주고', '아플 때 약을 챙겨줬다.', 'care', 12, FALSE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
