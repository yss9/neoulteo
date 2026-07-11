CREATE TABLE IF NOT EXISTS saved_places (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    attraction_content_id INT NOT NULL,
    source_type VARCHAR(30) DEFAULT 'ATTRACTION',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_places_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_saved_places_attraction
        FOREIGN KEY (attraction_content_id)
        REFERENCES attractions (content_id)
        ON DELETE RESTRICT,
    CONSTRAINT uk_saved_places_user_attraction
        UNIQUE (user_id, attraction_content_id)
);
