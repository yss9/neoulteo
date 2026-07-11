-- Demo seed data for local presentation/testing.
-- Login password for all demo users is: password
-- This file is intentionally idempotent. It can be executed repeatedly.

INSERT IGNORE INTO users (id, email, name, password) VALUES
    (9001, 'demo1@neoulteo.test', '여행러 민서', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9002, 'demo2@neoulteo.test', '바다러버 하윤', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9003, 'demo3@neoulteo.test', '맛집헌터 지훈', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9004, 'demo4@neoulteo.test', '가족여행 대장', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9005, 'demo5@neoulteo.test', '야경수집 하린', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9006, 'demo6@neoulteo.test', '코스설계 유준', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9007, 'demo7@neoulteo.test', '카페투어 수아', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9008, 'demo8@neoulteo.test', '도보여행 준호', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9009, 'demo9@neoulteo.test', '사진여행 라온', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9010, 'demo10@neoulteo.test', '혼행러 시오', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9011, 'demo11@neoulteo.test', '아이동반 지아', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9012, 'demo12@neoulteo.test', '로컬탐방 도윤', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9013, 'demo13@neoulteo.test', '시장투어 서아', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9014, 'demo14@neoulteo.test', '문화산책 은우', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9015, 'demo15@neoulteo.test', '레포츠러 태오', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9016, 'demo16@neoulteo.test', '감성기록 나은', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9017, 'demo17@neoulteo.test', '주말여행 현우', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9018, 'demo18@neoulteo.test', '계획공유 유나', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9019, 'demo19@neoulteo.test', '전국도장 지호', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    (9020, 'demo20@neoulteo.test', '숨은명소 다인', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

INSERT IGNORE INTO sidos (id, sido_code, sido_name) VALUES
    (9001, 1, '서울'), (9002, 2, '인천'), (9003, 3, '대전'), (9004, 4, '대구'),
    (9005, 5, '광주'), (9006, 6, '부산'), (9007, 7, '울산'), (9008, 8, '세종특별자치시'),
    (9009, 31, '경기도'), (9010, 32, '강원특별자치도'), (9011, 33, '충청북도'), (9012, 34, '충청남도'),
    (9013, 35, '경상북도'), (9014, 36, '경상남도'), (9015, 37, '전북특별자치도'), (9016, 38, '전라남도'),
    (9017, 39, '제주도');

INSERT IGNORE INTO guguns (id, sido_code, gugun_code, gugun_name) VALUES
    (9001, 1, 1, '강남구'), (9002, 1, 23, '종로구'), (9003, 6, 16, '해운대구'),
    (9004, 6, 12, '수영구'), (9005, 35, 23, '포항시'), (9006, 39, 4, '제주시'),
    (9007, 39, 3, '서귀포시'), (9008, 5, 3, '동구'), (9009, 4, 7, '수성구'),
    (9010, 31, 23, '용인시'), (9011, 32, 1, '강릉시'), (9012, 38, 13, '여수시');

INSERT IGNORE INTO contenttypes (content_type_id, content_type_name) VALUES
    (12, '관광지'), (14, '문화시설'), (15, '축제공연행사'), (25, '여행코스'),
    (28, '레포츠'), (32, '숙박'), (38, '쇼핑'), (39, '음식점');

INSERT IGNORE INTO attractions (
    id, content_id, title, content_type_id, area_code, si_gun_gu_code,
    first_image1, first_image2, mlevel, latitude, longitude, tel, addr1, addr2, homepage, overview
) VALUES
    (99001, 99001, '해운대해수욕장', 12, 6, 16, 'https://tong.visitkorea.or.kr/cms/resource/34/3090534_image2_1.JPG', '', 6, 35.1590840227225000, 129.1602785648270000, '', '부산광역시 해운대구 해운대해변로 264', '', '', '부산을 대표하는 바다 여행지로 산책, 야경, 주변 맛집을 함께 즐기기 좋다.'),
    (99002, 99002, '감천문화마을', 12, 6, 10, 'https://tong.visitkorea.or.kr/cms/resource/91/3365491_image2_1.jpg', '', 6, 35.0974606978369000, 129.0105969965340000, '', '부산광역시 사하구 감내2로 203', '', '', '알록달록한 골목과 전망이 이어지는 부산 대표 문화 산책 코스다.'),
    (99003, 99003, '영일대해수욕장', 12, 35, 23, 'https://tong.visitkorea.or.kr/cms/resource/79/4078979_image2_1.jpg', '', 6, 36.0550000000000000, 129.3779000000000000, '', '경상북도 포항시 북구 해안로', '', '', '포항 시내에서 접근하기 좋은 해변으로 바다와 카페를 함께 즐기기 좋다.'),
    (99004, 99004, '호미곶해맞이광장', 12, 35, 23, 'https://tong.visitkorea.or.kr/cms/resource/52/4056752_image2_1.jpg', '', 6, 36.0767810000000000, 129.5679700000000000, '', '경상북도 포항시 남구 호미곶면 대보리', '', '', '상생의 손과 일출 명소로 유명한 포항 대표 관광지다.'),
    (99005, 99005, '성산일출봉', 12, 39, 4, 'http://tong.visitkorea.or.kr/cms/resource/82/2944282_image2_1.bmp', '', 6, 33.4581111174000000, 126.9415156012000000, '', '제주특별자치도 서귀포시 성산읍 일출로 284-12', '', '', '제주 동쪽의 대표 풍경 명소로 일출과 바다 전망이 뛰어나다.'),
    (99006, 99006, '청계천광장', 12, 1, 23, 'https://tong.visitkorea.or.kr/cms/resource_photo/34/3538134_image2_1.jpg', '', 6, 37.5696470253000000, 127.0050743200000000, '', '서울특별시 종로구 창신동', '', '', '서울 도심에서 가볍게 산책하며 물길과 야경을 즐길 수 있는 공간이다.'),
    (99007, 99007, '광안리해수욕장', 12, 6, 12, 'http://tong.visitkorea.or.kr/cms/resource/45/3311245_image2_1.jpg', '', 6, 35.1538130576000000, 129.1185477876000000, '', '부산광역시 수영구 광안해변로 219', '', '', '광안대교 야경과 해변 산책을 함께 즐길 수 있는 부산 대표 명소다.'),
    (99008, 99008, '송도해상케이블카', 12, 6, 11, 'http://tong.visitkorea.or.kr/cms/resource/11/3413711_image2_1.jpg', '', 6, 35.0766810472000000, 129.0234248332000000, '', '부산광역시 서구 송도해변로 171', '', '', '바다 위를 지나며 부산 해안 풍경을 볼 수 있는 체험형 관광지다.'),
    (99009, 99009, '오륙도스카이워크', 12, 6, 4, 'https://tong.visitkorea.or.kr/cms/resource/69/3492369_image2_1.jpg', '', 6, 35.1006000000000000, 129.1244000000000000, '', '부산광역시 남구 오륙도로 137', '', '', '투명 바닥 전망대와 바다 풍경을 볼 수 있는 부산 해안 명소다.'),
    (99010, 99010, '이기대해안산책로', 12, 6, 4, 'https://tong.visitkorea.or.kr/cms/resource/02/3496802_image2_1.jpg', '', 6, 35.1154490000000000, 129.1235660000000000, '', '부산광역시 남구 용호동', '', '', '바다를 따라 걷는 산책 코스로 광안대교와 해안 절경이 좋다.'),
    (99011, 99011, '포항 스페이스워크', 12, 35, 23, 'https://tong.visitkorea.or.kr/cms/resource/63/3586563_image2_1.jpg', '', 6, 36.0652000000000000, 129.3903000000000000, '', '경상북도 포항시 북구 환호공원길 30', '', '', '철제 트랙을 따라 걷는 체험형 전망 명소다.'),
    (99012, 99012, '구룡포 일본인가옥거리', 12, 35, 23, 'https://tong.visitkorea.or.kr/cms/resource/11/4060811_image2_1.png', '', 6, 35.9906209551000000, 129.5604550366000000, '', '경상북도 포항시 남구 구룡포읍 호미로 277', '', '', '근대 건축과 골목 분위기를 함께 볼 수 있는 포항 산책 코스다.'),
    (99013, 99013, '죽도시장', 39, 35, 23, 'https://tong.visitkorea.or.kr/cms/resource/86/2678386_image2_1.jpg', '', 6, 36.0364190939505560, 129.3681412859447000, '', '경상북도 포항시 북구 죽도시장13길 13', '', '', '포항의 먹거리와 해산물을 한 번에 둘러볼 수 있는 전통시장이다.'),
    (99014, 99014, '한라산어리목탐방로', 12, 39, 4, 'https://tong.visitkorea.or.kr/cms/resource/94/4073694_image2_1.jpg', '', 6, 33.3929790000000000, 126.4953790000000000, '', '제주특별자치도 제주시 1100로 2070-61', '', '', '한라산의 숲과 능선을 만날 수 있는 제주 자연 탐방 코스다.'),
    (99015, 99015, '함덕해수욕장', 12, 39, 4, 'http://tong.visitkorea.or.kr/cms/resource/00/3354600_image2_1.jpg', '', 6, 33.5439000013000000, 126.6683567720000000, '', '제주특별자치도 제주시 조천읍 조함해안로 519-10', '', '', '맑은 물빛과 얕은 해변으로 가족 여행객에게 인기 있는 해수욕장이다.'),
    (99016, 99016, '동문시장', 39, 39, 4, 'http://tong.visitkorea.or.kr/cms/resource/38/2678438_image2_1.jpg', '', 6, 33.5115624162000000, 126.5260587646000000, '', '제주특별자치도 제주시 관덕로14길 20', '', '', '제주 먹거리와 기념품을 둘러보기 좋은 대표 시장이다.'),
    (99017, 99017, '남산서울타워', 12, 1, 24, '', '', 6, 37.5510545366352000, 126.9878820832950000, '', '서울특별시 용산구 남산공원길 105', '', '', '서울 전망과 야경을 보기 좋은 대표 전망 명소다.'),
    (99018, 99018, '경복궁', 14, 1, 23, 'https://tong.visitkorea.or.kr/cms/resource/98/3487598_image2_1.jpg', '', 6, 37.5760307000493940, 126.9767218660630600, '', '서울특별시 종로구 사직로 161', '', '', '서울의 역사와 궁궐 건축을 만날 수 있는 대표 문화 관광지다.'),
    (99019, 99019, '북촌한옥마을', 12, 1, 23, 'http://tong.visitkorea.or.kr/cms/resource/04/3304404_image2_1.jpg', '', 6, 37.5790529392000000, 126.9867060298000000, '', '서울특별시 종로구 계동길 37', '', '', '한옥 골목과 서울 도심 풍경을 함께 볼 수 있는 산책 명소다.'),
    (99020, 99020, '한강공원 여의도지구', 12, 1, 20, 'http://tong.visitkorea.or.kr/cms/resource/89/3544389_image2_1.jpg', '', 6, 37.5263997727000000, 126.9336095794000000, '', '서울특별시 영등포구 여의동로 330', '', '', '피크닉, 자전거, 야경을 즐기기 좋은 서울 대표 휴식 공간이다.'),
    (99021, 99021, '해운대 고흐의 길', 12, 6, 16, 'https://tong.visitkorea.or.kr/cms/resource/78/3492478_image2_1.jpg', '', 6, 35.1758950000000000, 129.1836480000000000, '', '부산광역시 해운대구 해운대로 898', '', '', '해운대 좌동에서 예술 작품과 산책을 함께 즐길 수 있는 거리다.'),
    (99022, 99022, '대구수목원', 12, 4, 2, '', '', 6, 35.8017000000000000, 128.5208000000000000, '', '대구광역시 달서구 화암로 342', '', '', '가볍게 산책하며 계절 식물을 보기 좋은 대구 자연 명소다.'),
    (99023, 99023, '수성못', 12, 4, 7, '', '', 6, 35.8294000000000000, 128.6168000000000000, '', '대구광역시 수성구 두산동', '', '', '호수 산책과 야경, 주변 카페를 즐기기 좋은 대구 명소다.'),
    (99024, 99024, '국립아시아문화전당', 14, 5, 3, '', '', 6, 35.1469000000000000, 126.9198000000000000, '', '광주광역시 동구 문화전당로 38', '', '', '전시, 공연, 문화 프로그램을 즐길 수 있는 광주 대표 문화 공간이다.'),
    (99025, 99025, '무등산 국립공원', 12, 5, 3, '', '', 6, 35.1341000000000000, 126.9888000000000000, '', '광주광역시 동구 증심사길 71', '', '', '광주를 대표하는 산과 자연 풍경을 만날 수 있는 국립공원이다.'),
    (99026, 99026, '강릉 안목해변', 12, 32, 1, '', '', 6, 37.7726000000000000, 128.9477000000000000, '', '강원특별자치도 강릉시 창해로14번길', '', '', '커피거리와 바다를 함께 즐기기 좋은 강릉 해변이다.'),
    (99027, 99027, '강릉 경포호', 12, 32, 1, '', '', 6, 37.7951000000000000, 128.8964000000000000, '', '강원특별자치도 강릉시 저동', '', '', '호수 산책과 자전거 코스로 인기 있는 강릉 대표 명소다.'),
    (99028, 99028, '여수 오동도', 12, 38, 13, '', '', 6, 34.7449000000000000, 127.7667000000000000, '', '전라남도 여수시 수정동', '', '', '동백숲과 바다 산책로가 어우러진 여수 대표 관광지다.'),
    (99029, 99029, '여수 낭만포차거리', 39, 38, 13, '', '', 6, 34.7391000000000000, 127.7357000000000000, '', '전라남도 여수시 종화동', '', '', '야경과 해산물 먹거리를 함께 즐기기 좋은 여수 먹거리 거리다.'),
    (99030, 99030, '에버랜드', 28, 31, 23, '', '', 6, 37.2940000000000000, 127.2026000000000000, '', '경기도 용인시 처인구 포곡읍 에버랜드로 199', '', '', '놀이기구와 동물원, 계절 축제를 함께 즐길 수 있는 가족 여행 명소다.'),
    (99031, 99031, '한국민속촌', 14, 31, 23, '', '', 6, 37.2592000000000000, 127.1212000000000000, '', '경기도 용인시 기흥구 민속촌로 90', '', '', '전통 문화 체험과 공연을 즐길 수 있는 문화시설이다.'),
    (99032, 99032, '서울빛초롱축제', 15, 1, 23, '', '', 6, 37.5696000000000000, 126.9785000000000000, '', '서울특별시 종로구 청계천 일대', '', '', '서울 도심에서 빛 조형물과 야간 산책을 즐길 수 있는 축제다.'),
    (99033, 99033, '부산바다축제', 15, 6, 16, '', '', 6, 35.1590000000000000, 129.1602000000000000, '', '부산광역시 해운대구 해운대해변로 일대', '', '', '부산의 여름 바다와 공연을 함께 즐길 수 있는 대표 축제다.'),
    (99034, 99034, '여수밤바다불꽃축제', 15, 38, 13, '', '', 6, 34.7390000000000000, 127.7350000000000000, '', '전라남도 여수시 이순신광장 일대', '', '', '여수 밤바다와 불꽃을 함께 즐길 수 있는 야간 축제다.'),
    (99035, 99035, '제주들불축제', 15, 39, 4, '', '', 6, 33.4090000000000000, 126.3920000000000000, '', '제주특별자치도 제주시 애월읍 봉성리', '', '', '제주의 자연과 전통을 주제로 한 대표 지역 축제다.');

INSERT IGNORE INTO hotplaces (user_id, attraction_content_id, visit_date, description)
SELECT u.id,
       a.content_id,
       DATE_ADD('2026-05-01', INTERVAL MOD(u.id + a.content_id, 55) DAY),
       CONCAT(u.name, '님이 추천한 ', a.title, '입니다. 사진 찍기 좋고 여행 계획에 넣기 좋은 검증된 장소입니다.')
FROM users u
JOIN attractions a ON a.content_id BETWEEN 99001 AND 99035
WHERE u.id BETWEEN 9001 AND 9020
  AND MOD(u.id + a.content_id, 4) IN (0, 1)
ON DUPLICATE KEY UPDATE
    visit_date = VALUES(visit_date),
    description = VALUES(description);

INSERT IGNORE INTO saved_places (user_id, attraction_content_id, source_type)
SELECT u.id,
       a.content_id,
       CASE WHEN MOD(u.id + a.content_id, 3) = 0 THEN 'HOTPLACE' ELSE 'ATTRACTION' END
FROM users u
JOIN attractions a ON a.content_id BETWEEN 99001 AND 99035
WHERE u.id BETWEEN 9001 AND 9020
  AND MOD(u.id + a.content_id, 5) IN (0, 2);

INSERT IGNORE INTO travel_plans (id, writer_email, title, duration_days, created_at, is_shared) VALUES
    ('DEMO-BUSAN-2D', 'demo1@neoulteo.test', '부산 바다와 골목 1박2일', 2, '2026-06-20', TRUE),
    ('DEMO-BUSAN-3D', 'demo6@neoulteo.test', '부산 해변 집중 2박3일', 3, '2026-06-12', TRUE),
    ('DEMO-BUSAN-WALK', 'demo10@neoulteo.test', '부산 도보 산책 코스', 2, '2026-06-17', TRUE),
    ('DEMO-POHANG-2D', 'demo2@neoulteo.test', '포항 물멍과 일출 코스', 2, '2026-06-21', TRUE),
    ('DEMO-POHANG-3D', 'demo8@neoulteo.test', '포항 바다와 시장 2박3일', 3, '2026-06-15', TRUE),
    ('DEMO-JEJU-1D', 'demo4@neoulteo.test', '제주 동쪽 당일 코스', 1, '2026-06-22', FALSE),
    ('DEMO-JEJU-2D', 'demo7@neoulteo.test', '제주 동쪽 바다 1박2일', 2, '2026-06-14', TRUE),
    ('DEMO-JEJU-FOOD', 'demo11@neoulteo.test', '제주 먹거리와 바다 코스', 2, '2026-06-18', TRUE),
    ('DEMO-SEOUL-2D', 'demo5@neoulteo.test', '서울 궁궐과 야경 1박2일', 2, '2026-06-13', TRUE),
    ('DEMO-SEOUL-PICNIC', 'demo9@neoulteo.test', '서울 피크닉 당일 코스', 1, '2026-06-16', FALSE),
    ('DEMO-DAEGU-1D', 'demo13@neoulteo.test', '대구 산책과 야경 코스', 1, '2026-06-19', TRUE),
    ('DEMO-GWANGJU-1D', 'demo14@neoulteo.test', '광주 문화와 자연 코스', 1, '2026-06-19', TRUE),
    ('DEMO-GANGNEUNG-2D', 'demo15@neoulteo.test', '강릉 커피와 호수 1박2일', 2, '2026-06-23', TRUE),
    ('DEMO-YEOSU-2D', 'demo16@neoulteo.test', '여수 밤바다 1박2일', 2, '2026-06-24', TRUE),
    ('DEMO-YONGIN-1D', 'demo17@neoulteo.test', '용인 가족 체험 코스', 1, '2026-06-25', TRUE);

INSERT IGNORE INTO travel_plan_places (plan_id, day_no, place_order, place_name, attraction_content_id) VALUES
    ('DEMO-BUSAN-2D', 1, 1, '해운대해수욕장', 99001), ('DEMO-BUSAN-2D', 1, 2, '광안리해수욕장', 99007), ('DEMO-BUSAN-2D', 2, 3, '감천문화마을', 99002),
    ('DEMO-BUSAN-3D', 1, 1, '해운대해수욕장', 99001), ('DEMO-BUSAN-3D', 2, 2, '오륙도스카이워크', 99009), ('DEMO-BUSAN-3D', 3, 3, '송도해상케이블카', 99008),
    ('DEMO-BUSAN-WALK', 1, 1, '이기대해안산책로', 99010), ('DEMO-BUSAN-WALK', 2, 2, '고흐의 길', 99021),
    ('DEMO-POHANG-2D', 1, 1, '영일대해수욕장', 99003), ('DEMO-POHANG-2D', 2, 2, '호미곶해맞이광장', 99004),
    ('DEMO-POHANG-3D', 1, 1, '영일대해수욕장', 99003), ('DEMO-POHANG-3D', 2, 2, '포항 스페이스워크', 99011), ('DEMO-POHANG-3D', 3, 3, '죽도시장', 99013),
    ('DEMO-JEJU-1D', 1, 1, '성산일출봉', 99005), ('DEMO-JEJU-2D', 1, 1, '성산일출봉', 99005), ('DEMO-JEJU-2D', 2, 2, '함덕해수욕장', 99015),
    ('DEMO-JEJU-FOOD', 1, 1, '동문시장', 99016), ('DEMO-JEJU-FOOD', 2, 2, '함덕해수욕장', 99015),
    ('DEMO-SEOUL-2D', 1, 1, '경복궁', 99018), ('DEMO-SEOUL-2D', 1, 2, '북촌한옥마을', 99019), ('DEMO-SEOUL-2D', 2, 3, '남산서울타워', 99017),
    ('DEMO-SEOUL-PICNIC', 1, 1, '한강공원 여의도지구', 99020),
    ('DEMO-DAEGU-1D', 1, 1, '대구수목원', 99022), ('DEMO-DAEGU-1D', 1, 2, '수성못', 99023),
    ('DEMO-GWANGJU-1D', 1, 1, '국립아시아문화전당', 99024), ('DEMO-GWANGJU-1D', 1, 2, '무등산 국립공원', 99025),
    ('DEMO-GANGNEUNG-2D', 1, 1, '강릉 안목해변', 99026), ('DEMO-GANGNEUNG-2D', 2, 2, '강릉 경포호', 99027),
    ('DEMO-YEOSU-2D', 1, 1, '여수 오동도', 99028), ('DEMO-YEOSU-2D', 2, 2, '여수 낭만포차거리', 99029),
    ('DEMO-YONGIN-1D', 1, 1, '에버랜드', 99030), ('DEMO-YONGIN-1D', 1, 2, '한국민속촌', 99031);

INSERT IGNORE INTO posts (id, user_id, category, title, content, image_url, travel_plan_id, views, created_at) VALUES
    (93001, 9001, 'PLAN_SHARE', '부산 1박2일 바다 여행 코스 공유합니다', '해운대와 광안리를 먼저 보고 다음 날 감천문화마을을 천천히 걷는 코스입니다.', 'https://tong.visitkorea.or.kr/cms/resource/34/3090534_image2_1.JPG', 'DEMO-BUSAN-2D', 142, '2026-06-20 10:10:00'),
    (93002, 9002, 'PLAN_SHARE', '포항에서 물멍하고 일출까지 보는 코스', '영일대에서 바다를 보고 호미곶에서 일출을 보는 구성입니다.', 'https://tong.visitkorea.or.kr/cms/resource/79/4078979_image2_1.jpg', 'DEMO-POHANG-2D', 131, '2026-06-21 13:30:00'),
    (93003, 9003, 'REVIEW', '[핫플 추천] 해운대해수욕장', '접근성이 좋고 주변 맛집이 많아서 여행 초보자도 편하게 들르기 좋습니다.', 'https://tong.visitkorea.or.kr/cms/resource/34/3090534_image2_1.JPG', NULL, 118, '2026-06-18 19:20:00'),
    (93004, 9004, 'REVIEW', '[핫플 추천] 성산일출봉', '아침 일찍 방문하면 사람이 적고 정상에서 보는 바다 풍경이 좋습니다.', 'http://tong.visitkorea.or.kr/cms/resource/82/2944282_image2_1.bmp', NULL, 124, '2026-06-17 08:40:00'),
    (93005, 9005, 'PLAN_SHARE', '서울 궁궐과 야경 1박2일', '경복궁, 북촌, 남산을 묶은 서울 도심 코스입니다.', 'https://tong.visitkorea.or.kr/cms/resource/98/3487598_image2_1.jpg', 'DEMO-SEOUL-2D', 139, '2026-06-13 18:00:00'),
    (93006, 9006, 'PLAN_SHARE', '부산 해변만 골라본 2박3일', '해운대, 오륙도, 송도를 나누어 방문했습니다.', 'https://tong.visitkorea.or.kr/cms/resource/34/3090534_image2_1.JPG', 'DEMO-BUSAN-3D', 147, '2026-06-12 09:30:00'),
    (93007, 9007, 'PLAN_SHARE', '제주 동쪽 바다 코스 추천', '성산일출봉과 함덕해수욕장을 묶은 1박2일 코스입니다.', 'http://tong.visitkorea.or.kr/cms/resource/82/2944282_image2_1.bmp', 'DEMO-JEJU-2D', 134, '2026-06-14 10:20:00'),
    (93008, 9008, 'PLAN_SHARE', '포항 2박3일 바다와 시장 코스', '바다, 전망, 시장을 모두 챙긴 포항 코스입니다.', 'https://tong.visitkorea.or.kr/cms/resource/79/4078979_image2_1.jpg', 'DEMO-POHANG-3D', 151, '2026-06-15 17:10:00'),
    (93009, 9009, 'REVIEW', '[핫플 추천] 남산서울타워', '해 질 무렵 올라가면 서울 야경이 잘 보입니다.', NULL, NULL, 120, '2026-06-12 20:00:00'),
    (93010, 9010, 'REVIEW', '[핫플 추천] 광안리해수욕장', '광안대교 야경이 좋아 저녁 일정으로 추천합니다.', 'http://tong.visitkorea.or.kr/cms/resource/45/3311245_image2_1.jpg', NULL, 128, '2026-06-11 21:00:00'),
    (93011, 9011, 'REVIEW', '[핫플 추천] 함덕해수욕장', '아이와 함께 물놀이하기 좋고 주변 편의시설도 괜찮습니다.', 'http://tong.visitkorea.or.kr/cms/resource/00/3354600_image2_1.jpg', NULL, 123, '2026-06-10 16:40:00'),
    (93012, 9012, 'REVIEW', '[핫플 추천] 죽도시장', '포항 여행에서 식사 코스로 넣기 좋습니다.', 'https://tong.visitkorea.or.kr/cms/resource/86/2678386_image2_1.jpg', NULL, 119, '2026-06-09 13:10:00'),
    (93013, 9013, 'PLAN_SHARE', '대구 산책과 야경 코스', '대구수목원과 수성못을 묶은 당일 코스입니다.', NULL, 'DEMO-DAEGU-1D', 86, '2026-06-08 10:00:00'),
    (93014, 9014, 'PLAN_SHARE', '광주 문화와 자연 코스', '국립아시아문화전당과 무등산을 연결한 코스입니다.', NULL, 'DEMO-GWANGJU-1D', 90, '2026-06-07 14:40:00'),
    (93015, 9015, 'PLAN_SHARE', '강릉 커피와 호수 1박2일', '안목해변과 경포호를 묶어 여유롭게 다녀왔습니다.', NULL, 'DEMO-GANGNEUNG-2D', 93, '2026-06-06 09:15:00'),
    (93016, 9016, 'PLAN_SHARE', '여수 밤바다 1박2일', '오동도와 낭만포차거리를 함께 즐기는 코스입니다.', NULL, 'DEMO-YEOSU-2D', 98, '2026-06-05 22:05:00'),
    (93017, 9017, 'PLAN_SHARE', '용인 가족 체험 코스', '에버랜드와 한국민속촌을 같은 날에 넣은 가족 코스입니다.', NULL, 'DEMO-YONGIN-1D', 102, '2026-06-04 12:00:00'),
    (93018, 9018, 'FREE', '여행 계획 공유 코드 기능 좋네요', '마음에 드는 여행 코스를 코드로 가져올 수 있어서 편합니다.', NULL, NULL, 76, '2026-06-03 18:30:00'),
    (93019, 9019, 'FREE', 'AI 코스 평가 써본 후기', '장소가 너무 많은 날을 알려줘서 코스를 줄이는 데 도움이 됐습니다.', NULL, NULL, 82, '2026-06-02 11:50:00'),
    (93020, 9020, 'QNA', '부산 비 오는 날 어디가 좋을까요?', '실내 위주로 갈만한 부산 코스를 추천받고 싶습니다.', NULL, NULL, 63, '2026-06-01 19:20:00'),
    (93021, 9001, 'QNA', '제주 동쪽 하루 코스 가능할까요?', '성산과 함덕을 하루에 묶어도 괜찮은지 궁금합니다.', NULL, NULL, 58, '2026-05-31 09:00:00'),
    (93022, 9002, 'FREE', '지도에서 지역별 핫플 보는 기능 시연하기 좋아요', '메인에서 지역을 누르면 핫플이 바로 보여서 발표 흐름이 좋습니다.', NULL, NULL, 92, '2026-05-30 16:00:00'),
    (93023, 9003, 'REVIEW', '[핫플 추천] 오륙도스카이워크', '바람이 강한 날은 조심해야 하지만 바다 전망은 좋습니다.', 'https://tong.visitkorea.or.kr/cms/resource/69/3492369_image2_1.jpg', NULL, 66, '2026-05-29 12:30:00'),
    (93024, 9004, 'NOTICE', '시연용 데이터가 추가되었습니다', '핫플, 여행 계획, 커뮤니티 게시글, 댓글, 좋아요 데이터가 충분히 보이도록 추가했습니다.', NULL, NULL, 163, '2026-05-28 09:00:00');

INSERT IGNORE INTO post_comments (post_id, user_id, content, created_at)
SELECT p.id,
       u.id,
       CONCAT(u.name, '님의 댓글: 이 코스 참고해서 다음 여행 계획에 넣어볼게요.')
FROM posts p
JOIN users u ON u.id BETWEEN 9001 AND 9020
WHERE p.id BETWEEN 93001 AND 93024
  AND MOD(p.id + u.id, 6) IN (0, 2);

INSERT IGNORE INTO post_likes (post_id, user_id)
SELECT p.id, u.id
FROM posts p
JOIN users u ON u.id BETWEEN 9001 AND 9020
WHERE p.id BETWEEN 93001 AND 93024
  AND MOD(p.id + u.id, 4) IN (0, 1, 3);
