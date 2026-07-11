-- Migration: convert hotplaces to verified attraction-based records.
-- Run this only after backing up the local database.
-- Existing hotplaces are preserved in hotplaces_legacy before the new table is created.

START TRANSACTION;

ALTER TABLE users
    DROP PRIMARY KEY,
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD CONSTRAINT uk_users_email UNIQUE (email);

RENAME TABLE hotplaces TO hotplaces_legacy;

CREATE TABLE hotplaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    attraction_content_id INT NOT NULL,
    visit_date DATE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hotplaces_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_hotplaces_attraction
        FOREIGN KEY (attraction_content_id)
        REFERENCES attractions (content_id)
        ON DELETE RESTRICT,
    CONSTRAINT uk_hotplaces_user_attraction
        UNIQUE (user_id, attraction_content_id)
);

INSERT INTO hotplaces (user_id, attraction_content_id, visit_date, description)
SELECT u.id,
       a.content_id,
       STR_TO_DATE(h.visit_date, '%Y-%m-%d'),
       h.description
FROM hotplaces_legacy h
JOIN users u ON u.email = h.writer_email
JOIN attractions a ON a.title = h.name
ON DUPLICATE KEY UPDATE
    visit_date = VALUES(visit_date),
    description = VALUES(description);

COMMIT;
