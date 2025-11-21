-- 1. 초기화 (테이블 비우기 & ID 리셋)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE DISH_INGREDIENT;
TRUNCATE TABLE DISH_IMAGE;
TRUNCATE TABLE INTEREST;
TRUNCATE TABLE DISH;
TRUNCATE TABLE INGREDIENT;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. INGREDIENT (재료/옵션) 등록
-- SOURCE(소스), BASIC_OPTION(기본), ADDITIONAL_OPTION(추가)
INSERT INTO INGREDIENT (ID, NAME, UNIT_COST, PRODUCT_CATEGORY) VALUES
                                                                   (1, '매운 소스', 500, 'SOURCE'),
                                                                   (2, '간장 소스', 500, 'SOURCE'),
                                                                   (3, '기본 야채', 1000, 'BASIC_OPTION'),
                                                                   (4, '돼지 고기 (200g)', 4000, 'BASIC_OPTION'),
                                                                   (5, '소 고기 (200g)', 6000, 'BASIC_OPTION'),
                                                                   (6, '해산물 모둠', 5000, 'BASIC_OPTION'),
                                                                   (7, '치즈 추가', 1000, 'ADDITIONAL_OPTION'),
                                                                   (8, '계란 후라이', 500, 'ADDITIONAL_OPTION'),
                                                                   (9, '라면 사리', 1000, 'ADDITIONAL_OPTION');

-- 3. DISH (요리) 등록 (총 3개 예시)
-- A세트: 인기 1위, 추천 3위, 가격 10,000원 (기본구성 합계)
INSERT INTO DISH (ID, NAME, BASE_PRICE, POPULARITY_RANK, RECOMMEND_RANK) VALUES
    (1, '돼지고기 요리', 10000, 1, 3);

-- B세트: 인기 2위, 추천 1위, 가격 13,000원 (기본구성 합계)
INSERT INTO DISH (ID, NAME, BASE_PRICE, POPULARITY_RANK, RECOMMEND_RANK) VALUES
    (2, '소고기 요리', 13000, 2, 1);

-- C세트: 인기 3위, 추천 2위, 가격 6,500원 (저가)
INSERT INTO DISH (ID, NAME, BASE_PRICE, POPULARITY_RANK, RECOMMEND_RANK) VALUES
    (3, '해산물 요리', 6500, 3, 2);

-- 4. DISH_IMAGE (이미지) 등록
-- 각 요리당 대표(REPRESENTATIVE) 1장, 상세(DETAIL_INFO) 2장씩
INSERT INTO DISH_IMAGE (ID, PATH, SEQUENCE, IMAGE_TYPE, DISH_ID) VALUES
                                                                     (1, '/images/a_main.jpg', 1, 'REPRESENTATIVE', 1),
                                                                     (2, '/images/a_detail_1.jpg', 1, 'DETAIL_INFO', 1),
                                                                     (3, '/images/a_detail_2.jpg', 2, 'DETAIL_INFO', 1),
                                                                     (4, '/images/b_main.jpg', 1, 'REPRESENTATIVE', 2),
                                                                     (5, '/images/b_detail_1.jpg', 1, 'DETAIL_INFO', 2),
                                                                     (6, '/images/b_detail_2.jpg', 2, 'DETAIL_INFO', 2),
                                                                     (7, '/images/c_main.jpg', 1, 'REPRESENTATIVE', 3),
                                                                     (8, '/images/c_detail_1.jpg', 1, 'DETAIL_INFO', 3),
                                                                     (9, '/images/c_detail_2.jpg', 2, 'DETAIL_INFO', 3);

-- 5. DISH_INGREDIENT (요리 기본 구성 연결) 등록
-- A세트 (10,000) = 간장(500) + 야채(1000) + 돼지고기(4000 * 2) + 라면사리(1000 * 0 - 추가옵션 테스트용 안넣음)
INSERT INTO DISH_INGREDIENT (ID, QUANTITY, DISH_ID, INGREDIENT_ID) VALUES
                                                                       (1, 1, 1, 2), -- 간장 소스
                                                                       (2, 1, 1, 3), -- 기본 야채
                                                                       (3, 2, 1, 4); -- 돼지 고기 2개

-- B세트 (13,000) = 매운(500) + 야채(1000) + 소고기(6000 * 2) - 500원 할인 느낌? (가격은 Dish.basePrice 기준임)
INSERT INTO DISH_INGREDIENT (ID, QUANTITY, DISH_ID, INGREDIENT_ID) VALUES
                                                                       (4, 1, 2, 1),
                                                                       (5, 1, 2, 3),
                                                                       (6, 2, 2, 5);

-- C세트 (6,500) = 매운(500) + 야채(1000) + 해산물(5000 * 1)
INSERT INTO DISH_INGREDIENT (ID, QUANTITY, DISH_ID, INGREDIENT_ID) VALUES
                                                                       (7, 1, 3, 1),
                                                                       (8, 1, 3, 3),
                                                                       (9, 1, 3, 6);

-- 6. INTEREST (관심상품) 등록
-- 회원 ID 1번이 요리 1번(A세트)을 찜한 상태
INSERT INTO INTEREST (ID, MEMBER_ID, DISH_ID, STATUS, REGISTERED_AT) VALUES
    (1, 1, 1, 'ACTIVE', NOW());