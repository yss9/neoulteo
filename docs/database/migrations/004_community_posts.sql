-- 커뮤니티 게시판 테이블 추가
CREATE TABLE IF NOT EXISTS posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL, -- NOTICE, FREE, REVIEW, QNA, PLAN_SHARE
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    travel_plan_id VARCHAR(30), -- 카테고리가 PLAN_SHARE일 경우 연동되는 여행 계획 ID
    views INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_posts_travel_plan FOREIGN KEY (travel_plan_id) REFERENCES travel_plans (id) ON DELETE SET NULL
);
