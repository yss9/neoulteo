-- Local development seed data only.
-- Do not run this script against production or shared databases.
-- This script assumes the attraction-based hotplaces schema is applied.

INSERT INTO users (email, name, password)
VALUES ('neoulteo.tester@example.com', 'Neoulteo Tester', '$2a$10$PypIBZHxWO6rxvtFmvkcreKRimleySl7kNnb0MZwhQph1g8M6tDE2')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    password = VALUES(password);

INSERT INTO hotplaces (user_id, attraction_content_id, visit_date, description)
SELECT seeded_user.id,
       ranked.content_id,
       DATE_ADD('2026-06-01', INTERVAL ranked.row_no - 1 DAY),
       CONCAT(ranked.title, '은(는) 공공 관광 데이터에서 선택한 검증된 장소입니다.')
FROM (
    SELECT content_id,
           title,
           area_code,
           ROW_NUMBER() OVER (PARTITION BY area_code ORDER BY content_id) AS row_no
    FROM attractions
    WHERE title IS NOT NULL
      AND latitude IS NOT NULL
      AND longitude IS NOT NULL
) ranked
JOIN users seeded_user ON seeded_user.email = 'neoulteo.tester@example.com'
WHERE ranked.row_no <= 5
ON DUPLICATE KEY UPDATE
    visit_date = VALUES(visit_date),
    description = VALUES(description);
