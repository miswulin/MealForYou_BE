-- -- Gemini 생성 테스트용 데이터 --
--
-- -- Dish 1번: 인기 1위, 추천 3위
-- INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
-- VALUES ('맛있는 밀키트 A', 15000, 1, 3);
--
-- -- Dish 2번: 인기 2위, 추천 1위
-- INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
-- VALUES ('고단백 밀키트 B', 18000, 2, 1);
--
-- -- Dish 3번: 인기 3위, 추천 2위
-- INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
-- VALUES ('저염 밀키트 C', 16500, 3, 2);
--
-- -- Dish 1번의 이미지들 (1번은 대표, 2번은 상세)
-- INSERT INTO dish_image (dish_id, path, sequence, image_type)
-- VALUES (1, 'https://example.com/images/mealkit_a_main.jpg', 1, 'REPRESENTATIVE');
--
-- INSERT INTO dish_image (dish_id, path, sequence, image_type)
-- VALUES (1, 'https://example.com/images/mealkit_a_detail.jpg', 2, 'DETAIL_INFO');
--
-- -- Dish 2번의 이미지들 (3번은 대표)
-- INSERT INTO dish_image (dish_id, path, sequence, image_type)
-- VALUES (2, 'https://example.com/images/mealkit_b_main.jpg', 1, 'REPRESENTATIVE');
--
-- -- Dish 3번: '저염 밀키트 C' (대표 이미지가 없는 케이스)
-- INSERT INTO dish_image (dish_id, path, sequence, image_type)
-- VALUES (3, 'https://example.com/images/mealkit_c_detail.jpg', 1, 'DETAIL_INFO');
--
-- -- Dish 4번: 인기 4위, 추천 4위
-- INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
-- VALUES ('비건 밀키트 D', 17000, 4, 4);
-- INSERT INTO dish_image (dish_id, path, sequence, image_type) VALUES (4, '...', 1, 'REPRESENTATIVE');
--
-- -- Dish 5번: 인기 5위, 추천 5위
-- INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
-- VALUES ('매콤 밀키트 E', 15500, 5, 5);
-- INSERT INTO dish_image (dish_id, path, sequence, image_type) VALUES (5, '...', 1, 'REPRESENTATIVE');
--
-- -- Dish 6번: 인기 6위, 추천 6위
-- INSERT INTO dish (name, base_price, popularity_rank, recommend_rank)
-- VALUES ('간편 밀키트 F', 14000, 6, 6);
-- INSERT INTO dish_image (dish_id, path, sequence, image_type) VALUES (6, '...', 1, 'REPRESENTATIVE');


SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE DISH_INGREDIENT;
TRUNCATE TABLE DISH_IMAGE;
TRUNCATE TABLE DISH;
TRUNCATE TABLE INGREDIENT;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. INGREDIENT (재료/옵션) 데이터
INSERT INTO INGREDIENT (ID, NAME, UNIT_COST, PRODUCT_CATEGORY, PRODUCT_TAG) VALUES
                                                                                (1, '매운 소스', 500, 'SOURCE', NULL),
                                                                                (2, '간장 소스', 500, 'SOURCE', NULL),
                                                                                (3, '기본 야채', 1000, 'BASIC_OPTION', NULL),
                                                                                (4, '기본 고기 (200g)', 4250, 'BASIC_OPTION', NULL),
                                                                                (5, '기본 해산물 (200g)', 5000, 'BASIC_OPTION', NULL),
                                                                                (6, '치즈 추가', 1000, 'ADDITIONAL_OPTION', NULL),
                                                                                (7, '계란 후라이 추가', 500, 'ADDITIONAL_OPTION', NULL),
                                                                                (8, '면 추가', 1500, 'ADDITIONAL_OPTION', NULL);


-- 2. DISH (요리) 데이터
-- A-SET (1): 간장소스(500*1) + 기본야채(1000*1) + 기본고기(4250*2) = 10000
INSERT INTO DISH (ID, NAME, BASE_PRICE, POPULARITY_RANK, RECOMMEND_RANK) VALUES
    (1, 'A-SET (고기)', 10000, 1, 2);

-- B-SET (2): 매운소스(500*1) + 기본야채(1000*1) + 기본해산물(5000*1) = 6500
INSERT INTO DISH (ID, NAME, BASE_PRICE, POPULARITY_RANK, RECOMMEND_RANK) VALUES
    (2, 'B-SET (해산물)', 6500, 2, 1);


-- 3. DISH_IMAGE (요리 이미지) 데이터 (이전과 동일)
INSERT INTO DISH_IMAGE (ID, PATH, SEQUENCE, IMAGE_TYPE, DISH_ID) VALUES
                                                                     (1, '/images/a_set_main.jpg', 1, 'REPRESENTATIVE', 1),
                                                                     (2, '/images/a_set_detail_1.jpg', 1, 'DETAIL_INFO', 1),
                                                                     (3, '/images/a_set_detail_2.jpg', 2, 'DETAIL_INFO', 1),
                                                                     (4, '/images/b_set_main.jpg', 1, 'REPRESENTATIVE', 2),
                                                                     (5, '/images/b_set_detail_1.jpg', 1, 'DETAIL_INFO', 2);


-- 4. DISH_INGREDIENT (요리-재료 연결) 데이터
-- A-SET (Dish 1)의 기본 구성
INSERT INTO DISH_INGREDIENT (ID, QUANTITY, DISH_ID, INGREDIENT_ID) VALUES
                                                                       (1, 1, 1, 2), -- A-SET은 '간장 소스'(ID 2) 1개
                                                                       (2, 1, 1, 3), -- A-SET은 '기본 야채'(ID 3) 1개
                                                                       (3, 2, 1, 4); -- A-SET은 '기본 고기'(ID 4) 2개 (4250 * 2)

-- B-SET (Dish 2)의 기본 구성
INSERT INTO DISH_INGREDIENT (ID, QUANTITY, DISH_ID, INGREDIENT_ID) VALUES
                                                                       (4, 1, 2, 1), -- B-SET은 '매운 소스'(ID 1) 1개
                                                                       (5, 1, 2, 3), -- B-SET은 '기본 야채'(ID 3) 1개
                                                                       (6, 1, 2, 5); -- B-SET은 '기본 해산물'(ID 5) 1개