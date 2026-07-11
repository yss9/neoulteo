CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS sidos (
    id INT PRIMARY KEY,
    sido_code INT NOT NULL UNIQUE,
    sido_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS guguns (
    id INT PRIMARY KEY,
    sido_code INT NOT NULL,
    gugun_code INT NOT NULL,
    gugun_name VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_guguns_code (sido_code, gugun_code)
);

CREATE TABLE IF NOT EXISTS contenttypes (
    content_type_id INT PRIMARY KEY,
    content_type_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS attractions (
    id INT PRIMARY KEY,
    content_id INT NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    content_type_id INT,
    area_code INT,
    si_gun_gu_code INT,
    first_image1 VARCHAR(1000),
    first_image2 VARCHAR(1000),
    mlevel INT,
    latitude DECIMAL(20, 16),
    longitude DECIMAL(20, 16),
    tel VARCHAR(255),
    addr1 VARCHAR(500),
    addr2 VARCHAR(500),
    homepage TEXT,
    overview TEXT
);

CREATE TABLE IF NOT EXISTS hotplaces (
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

CREATE TABLE IF NOT EXISTS travel_plans (
    id VARCHAR(30) PRIMARY KEY,
    writer_email VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    duration_days INT NOT NULL DEFAULT 1,
    created_at VARCHAR(50) NOT NULL,
    is_shared BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS travel_plan_places (
    plan_id VARCHAR(30) NOT NULL,
    day_no INT NOT NULL DEFAULT 1,
    place_order INT NOT NULL,
    place_name VARCHAR(255) NOT NULL,
    attraction_content_id INT,
    PRIMARY KEY (plan_id, place_order),
    CONSTRAINT fk_travel_plan_places_plan
        FOREIGN KEY (plan_id)
        REFERENCES travel_plans (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_travel_plan_places_attraction
        FOREIGN KEY (attraction_content_id)
        REFERENCES attractions (content_id)
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS tour_batch_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_date VARCHAR(8) NOT NULL,
    area_code VARCHAR(20),
    content_type_id VARCHAR(20),
    item_count INT NOT NULL DEFAULT 0,
    new_item_count INT NOT NULL DEFAULT 0,
    changed_item_count INT NOT NULL DEFAULT 0,
    missing_item_count INT NOT NULL DEFAULT 0,
    unchanged_item_count INT NOT NULL DEFAULT 0,
    change_summary TEXT,
    ai_summary TEXT,
    pdf_path VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL, -- NOTICE, FREE, REVIEW, QNA, PLAN_SHARE
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(1000),
    travel_plan_id VARCHAR(30), -- 카테고리가 PLAN_SHARE일 경우 연동되는 여행 계획 ID
    views INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_travel_plan FOREIGN KEY (travel_plan_id) REFERENCES travel_plans (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS post_likes (
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

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
